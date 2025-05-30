from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas
from utils.session_check import *

router = APIRouter(prefix="", tags=["Basket"])

# 🔁 Helper function to serialize basket items
def serialize_basket_items(user_id: int, db: Session) -> List[schemas.BasketItem]:
    items = db.query(models.Basket).filter(models.Basket.user_id == user_id).all()
    return [
        schemas.BasketItem(
            product_id=item.product.id,
            img=item.product.image_url,
            name=item.product.name,
            price=item.product.price,
            number=item.quantity
        )
        for item in items
    ]

# 🧺 Get Basket
@router.post("/Basket", response_model=List[schemas.BasketItem])
def get_basket(payload: schemas.BasketRequest,db: Session = Depends(get_db)):
	
	user = check_session(db,payload.session_id)
	return serialize_basket_items(user.id, db)

# ➕ Add to Basket
@router.post("/BasketAdd", response_model=List[schemas.BasketItem])
def add_to_basket(payload: schemas.BasketModifyRequest,db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	basket_item = db.query(models.Basket).filter_by(user_id=user.id, product_id=payload.product_id).first()
	if basket_item:
		basket_item.quantity += 1
	else:
		basket_item = models.Basket(user_id=user.id, product_id=payload.product_id, quantity=1)
		db.add(basket_item)
	db.commit()
	return serialize_basket_items(user.id, db)

# ➖ Subtract from Basket
@router.post("/BasketSub", response_model=List[schemas.BasketItem])
def subtract_from_basket(payload: schemas.BasketModifyRequest,db: Session = Depends(get_db)):

	user = check_session(db, payload.session_id)
	basket_item = db.query(models.Basket).filter_by(user_id=user.id, product_id=payload.product_id).first()
	if not basket_item or basket_item.quantity <= 0:
		raise HTTPException(status_code=400, detail="Item not in basket")
	basket_item.quantity -= 1
	if basket_item.quantity == 0:
		db.delete(basket_item)
	db.commit()
	return serialize_basket_items(user.id, db)

# ❌ Delete from Basket
@router.post("/BasketDel", response_model=List[schemas.BasketItem])
def delete_from_basket(payload: schemas.BasketModifyRequest, db: Session = Depends(get_db)):

	user = check_session(db, payload.session_id)
	basket_item = db.query(models.Basket).filter_by(user_id=user.id, product_id=payload.product_id).first()
	if basket_item:
		db.delete(basket_item)
		db.commit()
	return serialize_basket_items(user.id, db)

# 🔽 Insert one item directly
@router.post("/BasketInsert", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	new_item = models.Basket(user_id=user.id, product_id=payload.product_id, quantity=1)
	db.add(new_item)
	db.commit()
	return {"detail": "Inserted successfully"}


@router.post("/BasketInsert/Popular", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	product = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()
	new_item = models.Basket(user_id=user.id, product_id=re_product.id, quantity=1)
	db.add(new_item)
	db.commit()
	return {"detail": "Inserted successfully"}

@router.post("/BasketInsert/BigSale", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	product = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()
	new_item = models.Basket(user_id=user.id, product_id=re_product.id, quantity=1)
	db.add(new_item)
	db.commit()
	return {"detail": "Inserted successfully"}

@router.post("/BasketInsert/TodaySale", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	product = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()
	new_item = models.Basket(user_id=user.id, product_id=re_product.id, quantity=1)
	db.add(new_item)
	db.commit()
	return {"detail": "Inserted successfully"}

@router.post("/BasketInsert/New", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, db: Session = Depends(get_db)):

	user = check_session(db,payload.session_id)
	product = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()

	re_product = db.query(models.Product).filter(models.Product.name == product.name).first()
	new_item = models.Basket(user_id=user.id, product_id=re_product.id, quantity=1)
	db.add(new_item)
	db.commit()
	return {"detail": "Inserted successfully"}


