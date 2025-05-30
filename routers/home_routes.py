from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from schemas import TodaySaleItemResponse
from database import get_db
import models, schemas
from models import Product
from schemas import ProductSummary, SearchRequest 

router = APIRouter(prefix="", tags=["Home"])

@router.get("/Home", response_model=List[schemas.ProductSummary])
def read_home(db: Session = Depends(get_db)):
    product_ids = []

    product_ids += [item for item in db.query(models.PopularItem).limit(10)]
    product_ids += [item for item in db.query(models.BigSaleItem).limit(10)]
    product_ids += [item for item in db.query(models.TodaySaleItem).limit(10)]
    product_ids += [item for item in db.query(models.NewItem).limit(10)]

    if not product_ids:
        raise HTTPException(status_code=404, detail="No items found")

#products = db.query(models.Product).filter(models.Product.id.in_(product_ids)).all()
    return product_ids

@router.get("/PopluarItems", response_model=List[schemas.ProductSummary])
def read_popular(db: Session = Depends(get_db)):
    items = db.query(models.PopularItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Popular items not found")
      
    return items


@router.get("/BigSaleItems", response_model=List[schemas.ProductSummary])
def read_bigsale(db: Session = Depends(get_db)):
    items = db.query(models.BigSaleItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Big sale items not found")
    return items


@router.get("/TodaySaleItems", response_model=List[schemas.ProductSummary])
def read_today_sale(db: Session = Depends(get_db)):
    items = db.query(models.TodaySaleItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="Today's sale items not found")
    return items


@router.get("/NewItems", response_model=List[schemas.ProductSummary])
def read_new_items(db: Session = Depends(get_db)):
    items = db.query(models.NewItem).limit(20).all()
    if not items:
        raise HTTPException(status_code=404, detail="New items not found")
    return items
@router.post("/SearchItems", response_model=list[ProductSummary])
async def search_items(request: SearchRequest, db: Session = Depends(get_db)):
    keyword = request.search.lower()
    results = db.query(Product).filter(Product.name.ilike(f"%{keyword}%")).all()
    return results
