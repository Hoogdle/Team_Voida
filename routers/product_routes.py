from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas

router = APIRouter(prefix="", tags=["Product"])

@router.post("/ProductInfo", response_model=schemas.ProductDetail)
def product_info(payload: schemas.ProductIDRequest, db: Session = Depends(get_db)):
    
    prod = db.query(models.Product).filter(models.Product.id == payload.product_id).first()
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

   
    return schemas.ProductDetail(
        product_id=prod.id,
        name=prod.name,
        image_url=prod.image_url,
        description=prod.description,
        category=prod.category,
        price=float(prod.price)  
    )



@router.post("/CategoriesItems/{category}", response_model=List[schemas.ProductSummary])
def category_items(
    category: str = Path(..., description="Category name"),
    db: Session = Depends(get_db),
):
    """
    Return 20 items belonging to the given category
    """
    results = (
        db.query(models.Product)
        .filter(models.Product.category == category)
        .limit(20)
        .all()
    )
    if not results:
        raise HTTPException(status_code=404, detail=f"No items in category '{category}'")

    return [
        schemas.ProductSummary(
            product_id=item.id,
            img=item.image_url,
            name=item.name,
            price=item.price,
        )
        for item in results
    ]
