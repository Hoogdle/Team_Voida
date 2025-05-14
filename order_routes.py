from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database import get_db
from auth import create_access_token
from models import Purchase
from schemas import PurchaseRequest
from jose import jwt, JWTError
import os
from schemas import OrderCreateRequest
from dotenv import load_dotenv
load_dotenv()
JWT_SECRET = os.getenv("JWT_SECRET")
ALGORITHM = "HS256"

router = APIRouter()

@router.post("/purchase")
def make_purchase(data: PurchaseRequest, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(data.token, JWT_SECRET, algorithms=[ALGORITHM])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    purchase = Purchase(user_id=user_id, product_id=data.product_id)
    db.add(purchase)
    db.commit()
    return {"message": "Purchase recorded"}
from models import CartItem, Order, OrderItem

@router.post("/order")
def create_order(data: OrderCreateRequest, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(data.token, JWT_SECRET, algorithms=["HS256"])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    cart_items = db.query(CartItem).filter(CartItem.user_id == user_id).all()
    if not cart_items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    total = sum([item.quantity * 10.0 for item in cart_items])  # placeholder price logic
    order = Order(user_id=user_id, total_price=total)
    db.add(order)
    db.commit()
    db.refresh(order)

    for item in cart_items:
        order_item = OrderItem(order_id=order.id, product_id=item.product_id, quantity=item.quantity, price=10.0)
        db.add(order_item)
        db.delete(item)
    db.commit()

    return {"message": "Order created"}
@router.get("/order-history")
def get_order_history(token: str, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    orders = db.query(Order).filter(Order.user_id == user_id).all()
    return orders
