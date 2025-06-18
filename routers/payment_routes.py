from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
from utils.session_check import *

router = APIRouter(prefix="", tags=["Payment"])

# 장바구니에 결제 시도시 상품 정보를 제공하는 함수
@router.post("/BasketPayment", response_model=schemas.PaymentResponse)
def basket_payment(payload: schemas.BasketPayment, db: Session = Depends(get_db)):

	# 세션 아이디를 통해 유저 정보확인
	user = check_session(db,payload.session_id)
	basket_items = db.query(models.Basket).filter(models.Basket.user_id == user.id).all()

	if not basket_items:
		raise HTTPException(status_code=404, detail="No items in basket")
	
	items = [
        schemas.BasketItem(
            product_id=item.product.id,
            img=item.product.image_url,
            name=item.product.name,
            price=item.product.price,
            number=item.quantity
        )
        for item in basket_items
    ]

	# 배송지 정보와 추출된 상품 정보 제공
	# 배송지 정보 및 연락처 정보는 추후 구현 필요
	return schemas.PaymentResponse(
        address= "서울특별시 서대문구 북아현로 12",
        phone= "010-1234-5678",
        email= "xodud7737@gmail.com",
        item=items
    )

# 상품 정보 페이지에서 결제요청시 사용되는 함수
@router.post("/OneItemPayment", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(payload.product_id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )



@router.post("/OneItemPayment", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(payload.product_id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )


# 단일 상품 중 인기상품 결제시 사용되는 함수
# BigSale, New... 모두 같은 로직 사용
# DB를 초기 다소 복잡하게 설계하여 로직 또한 약간 꼬이는 문제발생
@router.post("/OneItemPayment/Popular", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)

	product = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(re_product.id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )



@router.post("/OneItemPayment/BigSale", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)

	product = db.query(models.BigSaleItem).filter(models.PopularItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(re_product.id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )



@router.post("/OneItemPayment/TodaySale", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)

	product = db.query(models.TodaySaleItem).filter(models.PopularItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(re_product.id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )


@router.post("/OneItemPayment/New", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)

	product = db.query(models.NewItem).filter(models.PopularItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

	profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

	if not profile:
		raise HTTPException(status_code=404, detail="User profile not found")

	prod = db.query(models.Product).get(re_product.id)
	if not prod:
		raise HTTPException(status_code=404, detail="Product not found")

	return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )

