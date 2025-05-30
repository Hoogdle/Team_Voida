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
from llava.model.language_model.llava_qwen import LlavaQwenForCausalLM
from llava.mm_utils import tokenizer_image_token, process_images
import requests
from PIL import Image

router = APIRouter(prefix="", tags=["Product"])


vlm_model_name = "NCSOFT/VARCO-VISION-14B"
tokenizer = AutoTokenizer.from_pretrained(vlm_model_name)
vlm_model = LlavaQwenForCausalLM.from_pretrained(
    vlm_model_name,
    torch_dtype=torch.float16,
    attn_implementation="flash_attention_2",
    low_cpu_mem_usage=True,
    device_map="auto"
)

vision_tower = vlm_model.get_vision_tower()
image_processor = vision_tower.image_processor
vlm_model = torch.compile(vlm_model)

# AI의 토큰값 설정
IMAGE_TOKEN_INDEX = -200
EOS_TOKEN = "<|im_end|>"

def call_ai(info, img):
	conversation = [
    {
        "role": "user",
        "content": [
            {"type": "text", "text": "주어진 이미지는 상품 설명 이미지야. 이미지 내에 존재하는 모든 정보를 간략하게 50자 이내의 문장으로 요약해줘. 최대한 시간을 적게 사용해서 요약해줘. 요약문은 상품 정보를 소개하는, 말하는 형태로 만들어줘. 아래에 상품의 정보를 추가로 기재할게 이를 참고해서 요약해줘" + info},
            {"type": "image"},
        ],
    },
    ]

	prompt = tokenizer.apply_chat_template(conversation, add_generation_prompt=True, tokenize=False)
    
	input_ids = tokenizer_image_token(prompt, tokenizer, IMAGE_TOKEN_INDEX, return_tensors="pt")

	input_ids = input_ids.unsqueeze(0).to(vlm_model.device)

	
	if(img[0] == '\"'): image_url = img[1:-1]
	else: image_url = img

	print(image_url)

	raw_image = Image.open(requests.get(image_url, stream=True).raw)
	image_tensors = process_images([raw_image], image_processor, vlm_model.config)

	image_tensors = [image_tensor.half().to(vlm_model.device) for image_tensor in image_tensors]

	image_sizes = [raw_image.size]

	with torch.inference_mode():
         output_ids = vlm_model.generate(
         input_ids,
         images=image_tensors,
         image_sizes=image_sizes,
         do_sample=False,
         max_new_tokens=1024,
         use_cache=True,
         )

	outputs = tokenizer.batch_decode(output_ids)[0]
	if outputs.endswith(EOS_TOKEN):
         outputs = outputs[: -len(EOS_TOKEN)]

	outputs = outputs.strip()
	return outputs

# ✅ Product Info by ID
@router.post("/ProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")
	
    ai_info = call_ai(prod.description, prod.image_url)
    

    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        price=float(prod.price),
# change below as ai result
        ai_info = ai_info,
        ai_review = ""
    )



# ✅ CategoriesItems
@router.get("/CategoriesItems/{category}", response_model=List[schemas.ProductSummary])
def category_items(category: str, db: Session = Depends(get_db)):
    category = "\"" + category + "\""
    results = db.query(models.Product).filter(models.Product.category == category).all()

    if not results:
        raise HTTPException(status_code=404, detail=f"No items in category '{category}'")

    return results

# Popular Items
@router.get("/PopularItems", response_model=List[schemas.ProductSummary])
def get_big_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.PopularItem).all()

    if not items:
        raise HTTPException(status_code=404, detail="No big sale items found")

    return items



# ✅ Big Sale Items
@router.get("/BigSaleItems", response_model=List[schemas.ProductSummary])
def get_big_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.BigSaleItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No big sale items found")

    return items


# ✅ Today Sale Items
@router.get("/TodaySaleItems", response_model=List[schemas.ProductSummary])
def get_today_sale_items(db: Session = Depends(get_db)):
    items = db.query(models.TodaySaleItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No today sale items found")

    return items


# ✅ New Items
@router.get("/NewItems", response_model=List[schemas.ProductSummary])
def get_new_items(db: Session = Depends(get_db)):
    items = db.query(models.NewItem).all()
    if not items:
        raise HTTPException(status_code=404, detail="No new items found")

    return items


# ✅ Product Info by ID
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


# ✅ Product Info by ID
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

# ✅ Product Info by ID
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

# ✅ Product Info by ID
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


