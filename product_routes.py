from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from database import get_db
from models import Product
from schemas import ProductCreate
from datetime import datetime
from sqlalchemy import or_

router = APIRouter()

@router.post("/products/")
def create_product(product: ProductCreate, db: Session = Depends(get_db)):
    db_product = Product(**product.dict())
    db.add(db_product)
    db.commit()
    db.refresh(db_product)
    return db_product

@router.get("/products/")
def read_products(db: Session = Depends(get_db)):
    return db.query(Product).all()

@router.get("/products/search")
def search_products(query: str = Query(...), db: Session = Depends(get_db)):
    return db.query(Product).filter(
        or_(
            Product.name.ilike(f"%{query}%"),
            Product.description.ilike(f"%{query}%"),
            Product.category.ilike(f"%{query}%")
        )
    ).all()

@router.get("/products/{product_id}")
def read_product(product_id: int, db: Session = Depends(get_db)):
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product
