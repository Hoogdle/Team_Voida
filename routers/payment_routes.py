from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from database import get_db
import models, schemas
from utils.session_check import get_current_user

router = APIRouter(prefix="", tags=["Payment"])

@router.post("/BasketPayment", response_model=schemas.PaymentResponse)
def basket_payment(user=Depends(get_current_user), db: Session = Depends(get_db)):
    """
    Returns user's address/contact + full basket contents
    """
    # Fetch user profile
    profile = db.query(models.UserProfile).filter(models.UserProfile.user_id == user.id).first()
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    items = db.query(models.Basket).filter(models.Basket.user_id == user.id).all()
    return schemas.PaymentResponse(
        address=profile.address,
        phone=profile.phone,
        email=profile.email,
        items=items,
    )

@router.post("/OneItemPayment", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    """
    Returns user's address/contact + single product in basket format
    """
    profile = db.query(models.UserProfile).filter(models.UserProfile.user_id == user.id).first()
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(payload.product_id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    item = schemas.BasketItem(
        product_id=prod.id,
        img=prod.image_url,
        name=prod.name,
        price=prod.price,
        number=1
    )
    return schemas.OneItemPaymentResponse(
        address=profile.address,
        phone=profile.phone,
        email=profile.email,
        item=item
    )
