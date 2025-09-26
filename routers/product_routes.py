from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas

import sys
sys.path.insert(1, "/home/xodud7737/AiApp/LLaVA-NeXT")
# vlm(ai)를 위한 라이브러리
import torch
from transformers import AutoTokenizer
import requests
from PIL import Image


# LLM Model 라이브러리
from transformers import AutoTokenizer, AutoModelForCausalLM, LlavaOnevisionForConditionalGeneration, AutoProcessor
import torch


router = APIRouter(prefix="", tags=["Product"])


vlm_model_name = "NCSOFT/VARCO-VISION-2.0-1.7B"

vlm_model = LlavaOnevisionForConditionalGeneration.from_pretrained(
    vlm_model_name,
    torch_dtype=torch.float16,
    attn_implementation="sdpa",
    device_map="auto"
)

processor = AutoProcessor.from_pretrained(vlm_model_name)

'''
vision_tower = vlm_model.get_vision_tower()
image_processor = vision_tower.image_processor
vlm_model = torch.compile(vlm_model)
'''

# LLM 세팅
start_token = "<|end_header_id|>"
end_token = "<|eot_id|>"


llm_model = AutoModelForCausalLM.from_pretrained(
      "NCSOFT/Llama-VARCO-8B-Instruct",
      torch_dtype=torch.bfloat16,
      device_map="auto"
  )
llm_tokenizer = AutoTokenizer.from_pretrained("NCSOFT/Llama-VARCO-8B-Instruct")

llm_model = torch.compile(llm_model)

# LLM 모델 호출 함수
def call_llm(review):
	# LLM 모델 프롬프트 설정 
	messages = [
      {"role": "system", "content": "너는 상품 요약을 수행하는 언어 모델이야"},
      {"role": "user", "content": "아래에 상품에 대한 리뷰 정보가 주어져있어. 주어진 리뷰 정보를 50자 이내의 문장으로, 리뷰를 요약해서 친절하게 존댓말과 함께  설명하는 형태로 요약문을 만들어줘. 최대한 간결하고 빠르게 핵심 정보만을 전달해줘." + review}
  ]

	inputs = llm_tokenizer.apply_chat_template(messages, return_tensors="pt").to(llm_model.device)
	

	eos_token_id = [
        llm_tokenizer.eos_token_id,
        llm_tokenizer.convert_tokens_to_ids("<|eot_id|>")
  ]

	outputs = llm_model.generate(
		inputs,
		eos_token_id=eos_token_id,
		max_length=8192
  )
	
	# AI output 전처리

	result = (llm_tokenizer.decode(outputs[0]))

	start_index = result.find(start_token) + len(start_token)
	start_index = result.find(start_token, start_index) + len(start_token)
	start_index = result.find(start_token ,start_index)+ len(start_token)

	end_index = result.find(end_token) + len(end_token)
	end_index = result.find(end_token,end_index) + len(end_token)
	end_index = result.find(end_token, end_index)

	result = result[start_index+2:end_index]
	result.replace('\n','')

	return result

@router.post("/Review", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):
	prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	review = call_llm(prod.product_review)

	
	return schemas.ReviewProvide(
			ai_review = review)

@router.post("/Review/Popular", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	review = call_llm(re_prod.product_review)

	
	return schemas.ReviewProvide(
			ai_review = review)

@router.post("/Review/BigSale", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	review = call_llm(re_prod.product_review)

	
	return schemas.ReviewProvide(
			ai_review = review)

@router.post("/Review/TodaySale", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	review = call_llm(re_prod.product_review)

	
	return schemas.ReviewProvide(
			ai_review = review)

@router.post("/Review/New", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	review = call_llm(re_prod.product_review)

	
	return schemas.ReviewProvide(
			ai_review = review)


# vlm 모델 호출 함수
def call_ai(info, img):
	# 모델에 사용되는 프롬프트 설정

	if(img[0] == '\"'): image_url = img[1:-1]
	else: image_url = img

	# For Debugging	
	print(image_url)

	conversation = [
    {
        "role": "user",
        "content": [
			{"type" : "image", "url" : image_url},
            {"type": "text", "text": "주어진 이미지는 상품 설명 이미지야. 이미지 내에 존재하는 모든 정보를 간략하게 100자 이내의 문장으로 요약해줘. 최대한 시간을 적게 사용해서 요약해줘. 요약문은 상품 정보를 친절하게 존댓말과 함께 소개하는 형태로 만들어줘."},
        ],
    },
    ]

	inputs = processor.apply_chat_template(
		conversation,
		add_generation_prompt = True,
		tokenize = True,
		return_dict = True,
		return_tensors = "pt"
	).to(vlm_model.device, torch.float16)
	
	generate_ids = vlm_model.generate(**inputs, max_new_tokens=512)
	generate_ids_trimmed = [
		out_ids[len(in_ids):] for in_ids, out_ids in zip(inputs.input_ids, generate_ids)
	]

	output = processor.decode(generate_ids_trimmed[0], skip_specail_tokens=True)
	
	return output


# 사용자가 요청 상품 정보의 아이디로 조회하여 해당 상품의 정보 제공
@router.post("/ProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.img_info)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )



# 카테고리 상품 정보 제공
# 현재는 '야채' 카테고리만 구현됨
@router.get("/CategoriesItems/{category}", response_model=List[schemas.ProductSummary])
def category_items(category: str, db: Session = Depends(get_db)):
    category = "\"" + category + "\""
    results = db.query(models.Product).filter(models.Product.category == category).all()

    if not results:
        raise HTTPException(status_code=404, detail=f"No items in category '{category}'")

    return results

# Popular Items
# 아래의 각 카테고리 로직은 모두 동일 Bigslae, New...
@router.get("/PopularItems", response_model=List[schemas.ProductSummary])
def get_big_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.PopularItem).all()

    if not items:
        raise HTTPException(status_code=404, detail="No big sale items found")

    return items



# Big Sale Items
@router.get("/BigSaleItems", response_model=List[schemas.ProductSummary])
def get_big_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.BigSaleItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No big sale items found")

    return items


# Today Sale Items
@router.get("/TodaySaleItems", response_model=List[schemas.ProductSummary])
def get_today_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.TodaySaleItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No today sale items found")

    return items


# New Items
@router.get("/NewItems", response_model=List[schemas.ProductSummary])
def get_new_items(db: Session = Depends(get_db)):
    items = db.query(models.NewItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No new items found")

    return items


# Product Info by ID
@router.post("/PopularProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.image_info)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )


# Product Info by ID
@router.post("/BigSaleProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.image_info)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )

# Product Info by ID
@router.post("/TodaySaleProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.image_info)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )

# Product Info by ID
@router.post("/NewProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.image_info)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )


