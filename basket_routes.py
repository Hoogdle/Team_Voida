from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas
from utils.session_check import get_current_user

router = APIRouter(prefix="", tags=["Basket"])

@router.post("/Basket", response_model=List[schemas.BasketItem])
def get_basket(user=Depends(get_current_user), db: Session = Depends(get_db)):
    items = db.query(models.Basket).filter(models.Basket.user_id == user.id).all()
    return items

@router.post("/BasketAdd", response_model=List[schemas.BasketItem])
def add_to_basket(payload: schemas.BasketModifyRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    basket_item = (
        db.query(models.Basket)
        .filter_by(user_id=user.id, product_id=payload.product_id)
        .first()
    )
    if basket_item:
        basket_item.quantity += 1
    else:
        basket_item = models.Basket(user_id=user.id, product_id=payload.product_id, quantity=1)
        db.add(basket_item)
    db.commit()
    return db.query(models.Basket).filter(models.Basket.user_id == user.id).all()

@router.post("/BasketSub", response_model=List[schemas.BasketItem])
def subtract_from_basket(payload: schemas.BasketModifyRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    basket_item = (
        db.query(models.Basket)
        .filter_by(user_id=user.id, product_id=payload.product_id)
        .first()
    )
    if not basket_item or basket_item.quantity <= 0:
        raise HTTPException(status_code=400, detail="Item not in basket")
    basket_item.quantity -= 1
    if basket_item.quantity == 0:
        db.delete(basket_item)
    db.commit()
    return db.query(models.Basket).filter(models.Basket.user_id == user.id).all()

@router.post("/BasketDel", response_model=List[schemas.BasketItem])
def delete_from_basket(payload: schemas.BasketModifyRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    basket_item = (
        db.query(models.Basket)
        .filter_by(user_id=user.id, product_id=payload.product_id)
        .first()
    )
    if basket_item:
        db.delete(basket_item)
        db.commit()
    return db.query(models.Basket).filter(models.Basket.user_id == user.id).all()

@router.post("/BasketInsert", response_model=dict)
def insert_to_basket(payload: schemas.BasketInsertRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    new_item = models.Basket(user_id=user.id, product_id=payload.product_id, quantity=1)
    db.add(new_item)
    db.commit()
    return {"detail": "Inserted successfully"}
