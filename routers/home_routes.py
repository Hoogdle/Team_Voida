from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas

router = APIRouter(prefix="", tags=["Home"])

@router.post("/Home", response_model=List[schemas.ProductSummary])
def read_home(db: Session = Depends(get_db)):
    product_ids = []

    product_ids += [item.id for item in db.query(models.PopularItem).all()]
    product_ids += [item.id for item in db.query(models.BigSaleItem).all()]
    product_ids += [item.id for item in db.query(models.TodaySaleItem).all()]
    product_ids += [item.id for item in db.query(models.NewItem).all()]

    if not product_ids:
        raise HTTPException(status_code=404, detail="No items found")

    products = db.query(models.Product).filter(models.Product.id.in_(product_ids)).all()
    return products

@router.post("/PopluarItems", response_model=List[schemas.ProductSummary])
def read_popular(db: Session = Depends(get_db)):
    items = db.query(models.PopularItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Popular items not found")
    return items


@router.post("/BigSaleItems", response_model=List[schemas.ProductSummary])
def read_bigsale(db: Session = Depends(get_db)):
    items = db.query(models.BigSaleItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Big sale items not found")
    return items


@router.post("/TodaySaleItems", response_model=List[schemas.ProductSummary])
def read_today_sale(db: Session = Depends(get_db)):
    items = db.query(models.TodaySaleItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Today's sale items not found")
    return items


@router.post("/NewItems", response_model=List[schemas.ProductSummary])
def read_new_items(db: Session = Depends(get_db)):
    items = db.query(models.NewItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="New items not found")
    return items
