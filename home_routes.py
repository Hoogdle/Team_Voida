from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas

router = APIRouter(prefix="", tags=["Home"])

@router.post("/Home", response_model=List[schemas.ProductSummary])
def read_home(db: Session = Depends(get_db)):
    """
    Returns 80 products divided into 4 groups of 20 each:
    PopularItems, BigSaleItems, TodaySaleItems, NewItems.
    """
    popular = db.query(models.PopularItem).limit(20).all()
    bigsale = db.query(models.BigSaleItem).limit(20).all()
    today = db.query(models.TodaySaleItem).limit(20).all()
    new = db.query(models.NewItem).limit(20).all()
    combined = popular + bigsale + today + new
    if not combined:
        raise HTTPException(status_code=404, detail="No items found")
    return combined


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
