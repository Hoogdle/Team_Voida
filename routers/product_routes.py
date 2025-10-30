from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session
from typing import List
import asyncio

from database import get_db
import models, schemas
import threading

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



stop_flags: dict[str, threading.Event] = {}

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
start_token = "[|assistant|]"
end_token = "[|endofturn|]"

llm_model_name = "LGAI-EXAONE/EXAONE-3.5-2.4B-Instruct"

llm_model = AutoModelForCausalLM.from_pretrained(
    llm_model_name,
    torch_dtype=torch.bfloat16,
    trust_remote_code=True,
    device_map="auto"
)
llm_tokenizer = AutoTokenizer.from_pretrained(llm_model_name)

llm_model = torch.compile(llm_model)



# Detailed Model
d_vlm_model_name = "NCSOFT/VARCO-VISION-2.0-14B"
d_vlm_model = LlavaOnevisionForConditionalGeneration.from_pretrained(
    d_vlm_model_name,
    torch_dtype=torch.float16,
    attn_implementation="sdpa",
    device_map="auto",
)
d_processor = AutoProcessor.from_pretrained(d_vlm_model_name)


# LLM 모델 호출 함수
def call_llm(review, return_list, stop_event):
	# LLM 모델 프롬프트 설정 
	messages = [
      {"role": "system", "content": "너는 상품 요약을 수행하는 언어 모델이야"},
      {"role": "user", "content": "아래에 상품에 대한 리뷰 정보가 주어져있어. 주어진 리뷰 정보를 50자 이내의 문장으로, 리뷰를 요약해서 친절하게 존댓말과 함께  설명하는 형태로 요약문을 만들어줘. 최대한 간결하고 빠르게 핵심 정보만을 전달해줘." + review}
  ]

	inputs = llm_tokenizer.apply_chat_template(messages, return_tensors="pt").to(llm_model.device)
	

	input_ids = llm_tokenizer.apply_chat_template(
    messages,
    tokenize=True,
    add_generation_prompt=True,
    return_tensors="pt"
	)

	output = llm_model.generate(
		input_ids = input_ids.to("cuda"),
		eos_token_id=llm_tokenizer.eos_token_id,
		max_new_tokens=128,
		stop_event = stop_event,
		do_sample=False,
	)	

	# AI output 전처리
	result = (llm_tokenizer.decode(output[0]))

	start_index = result.find(start_token) + len(start_token)

	end_index = result.find(end_token) + len(end_token)
	end_index = result.find(end_token, end_index)

	result = result[start_index:end_index]
	result.replace('\n','')

	return_list.append(result)

@router.post("/Review", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):
	prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	pid = payload.session_id + "_r"

	ai_info = []
	stop_event = threading.Event()

    # Call AI
	ai_process = threading.Thread(target = call_llm, args = (re_prod.product_review, ai_info, stop_event))

    # Setting Stop Flag
	stop_flags[pid] = stop_event
    
    # Start and Wait
	ai_process.start()
	ai_process.join()
	
	return schemas.ReviewProvide(
			ai_review = ai_info[0])

	
@router.post("/Review/Popular", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	pid = payload.session_id + "_r"

	ai_info = []
	stop_event = threading.Event()

    # Call AI
	ai_process = threading.Thread(target = call_llm, args = (re_prod.product_review, ai_info, stop_event))

    # Setting Stop Flag
	stop_flags[pid] = stop_event
    
    # Start and Wait
	ai_process.start()
	ai_process.join()
	
	return schemas.ReviewProvide(
			ai_review = ai_info[0])


@router.post("/Review/BigSale", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	pid = payload.session_id + "_r"

	ai_info = []
	stop_event = threading.Event()

    # Call AI
	ai_process = threading.Thread(target = call_llm, args = (re_prod.product_review, ai_info, stop_event))

    # Setting Stop Flag
	stop_flags[pid] = stop_event
    
    # Start and Wait
	ai_process.start()
	ai_process.join()
	
	return schemas.ReviewProvide(
			ai_review = ai_info[0])

@router.post("/Review/TodaySale", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	pid = payload.session_id + "_r"

	ai_info = []
	stop_event = threading.Event()

    # Call AI
	ai_process = threading.Thread(target = call_llm, args = (re_prod.product_review, ai_info, stop_event))

    # Setting Stop Flag
	stop_flags[pid] = stop_event
    
    # Start and Wait
	ai_process.start()
	ai_process.join()
	
	return schemas.ReviewProvide(
			ai_review = ai_info[0])

@router.post("/Review/New", response_model = schemas.ReviewProvide)
def review_info(payload: schemas.ReviewRequest, db: Session = Depends(get_db)):

	prod = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()
	re_prod = db.query(models.Product).filter(models.Product.name == prod.name).first()
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")
	
	pid = payload.session_id + "_r"

	ai_info = []
	stop_event = threading.Event()

    # Call AI
	ai_process = threading.Thread(target = call_llm, args = (re_prod.product_review, ai_info, stop_event))

    # Setting Stop Flag
	stop_flags[pid] = stop_event
    
    # Start and Wait
	ai_process.start()
	ai_process.join()
	
	return schemas.ReviewProvide(
			ai_review = ai_info[0])

# vlm 모델 호출 함수
def call_ai(name, info, img, return_list, stop_event):
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
            {"type": "text", "text": "상품명 : " +name+ "먼저, 주어진 상품 이미지 정보를 잘 분석해주고, 주요 정보만을 추출하여 500자 이내에 상품 설명문 만들어줘, 이미지의 내용을 그대로 읽는게 아니라  상품을 '설명하는' 느낌으로 핵심 정보만을 잘 추출해서 만들어줘. 주로 글자로 구성된 이미지의 경우에는 텍스트를 잘 추출해서 핵심 정보만을 살려서 요약문을 만들어줘, 정보를 요약해야 한다는거 잊지마."},
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
	
	generate_ids = vlm_model.generate(**inputs, max_new_tokens=512, stop_event=stop_event)
	generate_ids_trimmed = [
		out_ids[len(in_ids):] for in_ids, out_ids in zip(inputs.input_ids, generate_ids)
	]

	output = processor.decode(generate_ids_trimmed[0], skip_specail_tokens=True)
	
	return_list.append(output.strip("<|im_end|>"))

# Call Detailed VLM
def d_call_ai(name, info, img, return_list, stop_event):
	# 모델에 사용되는 프롬프트 설정

	if(img[0] == '\"'): image_url = img[1:-1]
	else: image_url = img


	conversation = [
    {
        "role": "user",
        "content": [
			{"type" : "image", "url" : image_url},
            {"type": "text", "text": "상품명 : " +name+ "먼저, 주어진 상품 이미지 정보를 잘 분석해주고, 주요 정보만을 추출하여 500자 이내에 상품 설명문 만들어줘, 이미지의 내용을 그대로 읽는게 아니라  상품을 '설명하는' 느낌으로 핵심 정보만을 잘 추출해서 만들어줘. 주로 글자로 구성된 이미지의 경우에는 텍스트를 잘 추출해서 핵심 정보만을 살려서 요약문을 만들어줘, 정보를 요약해야 한다는거 잊지마."},
        ],
    },
    ]

	inputs = d_processor.apply_chat_template(
		conversation,
		add_generation_prompt = True,
		tokenize = True,
		return_dict = True,
		return_tensors = "pt"
	).to(d_vlm_model.device, torch.float16)
	
	generate_ids = d_vlm_model.generate(**inputs, max_new_tokens=512, stop_event = stop_event)
	generate_ids_trimmed = [
		out_ids[len(in_ids):] for in_ids, out_ids in zip(inputs.input_ids, generate_ids)
	]

	output = d_processor.decode(generate_ids_trimmed[0], skip_specail_tokens=True)
	
	return_list.append(output.strip("<|im_end|>"))


# 사용자가 요청 상품 정보의 아이디로 조회하여 해당 상품의 정보 제공
@router.post("/ProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_p"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = call_ai, args = (prod.name,prod.description, prod.img_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )


# 사용자가 요청 상품 정보의 아이디로 조회하여 해당 상품의 정보 제공
@router.post("/ProductDetailedInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
    pid = payload.session_id + "_d"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = d_call_ai, args = (prod.name,prod.description, prod.img_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()
 

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
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

    pid = payload.session_id + "_p"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()
 
    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

@router.post("/PopularProductDetailedInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    pid = payload.session_id + "_d"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = d_call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )


# Product Info by ID
@router.post("/BigSaleProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_p"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

# Product Info by ID
@router.post("/BigSaleProductDetailedInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_d"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = d_call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

# Product Info by ID
@router.post("/TodaySaleProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_p"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

# Product Info By ID
@router.post("/TodaySaleProductDetailedInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_d"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = d_call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )


# Product Info by ID
@router.post("/NewProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_p"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

# Product Info by ID
@router.post("/NewProductDetailedInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    pid = payload.session_id + "_d"

    ai_info = []
    stop_event = threading.Event()

    # Call AI
    ai_process = threading.Thread(target = d_call_ai, args = (prod.name,prod.description, prod.image_info, ai_info, stop_event))

    # Setting Stop Flag
    stop_flags[pid] = stop_event
    
    # Start and Wait
    ai_process.start()
    ai_process.join()


    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info[0],
        ai_review = ""
    )

# Product Info by ID
@router.post("/CancelAI")
def product_info(payload: schemas.CancelAIRequest, db: Session = Depends(get_db)):
    session_id = payload.session_id
    session_product = session_id + "_p"
    session_detail = session_id + "_d"
    session_review = session_id + "_r"

    if session_product in stop_flags:
        stop_flags[session_product].set()
        print("cancel product info")
        del stop_flags[session_product]
    
    if session_detail in stop_flags:
        stop_flags[session_detail].set()
        print("cancel product deep")
        del stop_flags[session_detail]

    if session_review in stop_flags:
        stop_flags[session_review].set()
        print("cancel review info")
        del stop_flags[session_review]


