from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas

router = APIRouter(prefix="", tags=["Search"])

@router.post("/SearchItems", response_model=List[schemas.ProductSummary])
def search_items(payload: schemas.SearchRequest, db: Session = Depends(get_db)):
    """
    step1: receive search query
    step2: return up to 20 matching products
    """
    q = f"%{payload.search}%"
    results = (
        db.query(models.Product)
        .filter(models.Product.title.ilike(q))
        .limit(20)
        .all()
    )
    if not results:
        raise HTTPException(status_code=404, detail="No items match your search")
    # Map to summary schema
    return [
        schemas.ProductSummary(
            product_id=item.id,
            img=item.img,
            name=item.title,
            price=item.price,
        )
        for item in results
    ]
