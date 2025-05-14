from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from models import CartItem
from schemas import CartAddRequest
from database import get_db
from auth import JWT_SECRET
from jose import jwt, JWTError

router = APIRouter()

@router.post("/cart/add")
def add_to_cart(data: CartAddRequest, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(data.token, JWT_SECRET, algorithms=["HS256"])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    cart_item = CartItem(user_id=user_id, product_id=data.product_id, quantity=data.quantity)
    db.add(cart_item)
    db.commit()
    return {"message": "Item added to cart"}
