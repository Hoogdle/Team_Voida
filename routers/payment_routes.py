from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
from utils.session_check import *

router = APIRouter(prefix="", tags=["Payment"])

@router.post("/BasketPayment", response_model=schemas.PaymentResponse)
def basket_payment(payload: schemas.BasketPayment, db: Session = Depends(get_db)):

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

	return schemas.PaymentResponse(
        address= "서울특별시 서대문구 북아현로 12",
        phone= "010-1234-5678",
        email= "xodud7737@gmail.com",
        item=items
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

