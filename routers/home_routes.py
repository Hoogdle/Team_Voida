from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from schemas import TodaySaleItemResponse
from database import get_db
import models, schemas
from models import Product
from schemas import ProductSummary, SearchRequest 

router = APIRouter(prefix="", tags=["Home"])

# 홈 화면의 상품 정보 전달
@router.get("/Home", response_model=List[schemas.ProductSummary])
def read_home(db: Session = Depends(get_db)):
    # 모든 상품 정보를 담을 리스트
    product_ids = []

    
    prod1 = db.query(models.Home).filter(models.Home.sector == 1).all()
    prod2 = db.query(models.Home).filter(models.Home.sector == 2).all()
    prod3 = db.query(models.Home).filter(models.Home.sector == 3).all()
    prod4 = db.query(models.Home).filter(models.Home.sector == 4).all()

    # DB 쿼리를 활용해 각 카테고리별 상품을 리스트에 저장
    product_ids += [item for item in db.query(models.Product).filter(models.Product.id.in_(prod1.product_id)).limit(10)]
    product_ids += [item for item in db.query(models.Product).filter(models.Product.id.in_(prod2.product_id)).limit(10)]
    product_ids += [item for item in db.query(models.Product).filter(models.Product.id.in_(prod3.product_id)).limit(10)]
    product_ids += [item for item in db.query(models.Product).filter(models.Product.id.in_(prod4.product_id)).limit(10)]

    if not product_ids:
        raise HTTPException(status_code=404, detail="No items found")

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description = item.description,
            price = item.price,
            image_url = item.img,
            category = item.category,
        )
        for item in product_ids
    ]

    #products = db.query(models.Product).filter(models.Product.id.in_(product_ids)).all()
    return product_ids

#  인기 상품 카테고리에 속하는 상품 20개 제공
@router.get("/PopluarItems", response_model=List[schemas.ProductSummary])
def read_popular(db: Session = Depends(get_db)):

    items = db.query(models.Home).filter(models.Home.sector == 1).limit(20).all()

    if not items:
        raise HTTPException(status_code=404, detail="Popular items not found")
    
    filterd_id = [item.product_id for item in items]
    items = db.query(models.Product).filter(models.Product.id.in_(filterd_id)).limit(20)

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description=item.description,
            price = item.price,
            image_url=item.img,
            category=item.category
        )
        for item in items
    ]
      
    return items

#  할인 카테고리에 속하는 상품 20개 제공
@router.get("/BigSaleItems", response_model=List[schemas.ProductSummary])
def read_bigsale(db: Session = Depends(get_db)):
    items = db.query(models.Home).filter(models.Home.sector == 2).limit(20).all()

    if not items:
        raise HTTPException(status_code=404, detail="BigSaleItems items not found")
    
    filterd_id = [item.product_id for item in items]
    items = db.query(models.Product).filter(models.Product.id.in_(filterd_id)).limit(20)

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description=item.description,
            price = item.price,
            image_url=item.img,
            category=item.category
        )
        for item in items
    ]
      
    return items


@router.get("/TodaySaleItems", response_model=List[schemas.ProductSummary])
def read_today_sale(db: Session = Depends(get_db)):
    items = db.query(models.Home).filter(models.Home.sector == 3).limit(20).all()

    if not items:
        raise HTTPException(status_code=404, detail="TodaySaleItems items not found")
    
    filterd_id = [item.product_id for item in items]
    items = db.query(models.Product).filter(models.Product.id.in_(filterd_id)).limit(20)

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description=item.description,
            price = item.price,
            image_url=item.img,
            category=item.category
        )
        for item in items
    ]
      
    return items

#  신상품 카테고리에 속하는 상품 20개 제공
@router.get("/NewItems", response_model=List[schemas.ProductSummary])
def read_new_items(db: Session = Depends(get_db)):
    items = db.query(models.Home).filter(models.Home.sector == 3).limit(20).all()

    if not items:
        raise HTTPException(status_code=404, detail="NewItems items not found")
    
    filterd_id = [item.product_id for item in items]
    items = db.query(models.Product).filter(models.Product.id.in_(filterd_id)).limit(20)

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description=item.description,
            price = item.price,
            image_url=item.img,
            category=item.category
        )
        for item in items
    ]
      
    return items

# 상품 검색 요청 처리 및 상풍 제공
@router.post("/SearchItems", response_model=list[ProductSummary])
async def search_items(request: SearchRequest, db: Session = Depends(get_db)):
    keyword = request.search.lower()
    results = db.query(Product).filter(Product.title.ilike(f"%{keyword}%")).all()

    result = [
        schemas.ProductSummary(
            id = item.id,
            name = item.title,
            description=item.description,
            price=item.price,
            image_url=item.img,
            category=item.category
        )
        for item in results
    ]
    return results
