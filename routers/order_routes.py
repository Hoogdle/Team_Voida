from sys import path_hooks

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from database import get_db
import models, schemas
from utils.session_check import check_session

router = APIRouter(
    prefix="",
    tags=["Orders"]
)

# 주문 페이지 정보제공 
# TODO, 주문 실패 예외처리
@router.post("/CreateOrder", response_model=schemas.OrderResponse)
def create_order(order: schemas.OrderCreate, db: Session = Depends(get_db)):

    user = check_session(db, order.session_id)

    db_order = models.Order(
        user_id=user.id,
        total_price=order.total_price,
        address = user.address,
        phone = user.cell,
        email = user.email,
        is_cancel = False
    )

    db.add(db_order)
    db.commit()
    db.refresh(db_order)

    for item in order.items:
        db_item = models.OrderItem(
            order_id=db_order.id,
            product_id=item.product_id,
            quantity=item.quantity,
            price=item.price,
            is_cancel = False
        )
        db.add(db_item)
    db.commit()

    return schemas.OrderResponse(
        order_num = db_order.id,
        total_price = db_order.total_price,
        success = True
    )



@router.get("/CancelOrder", response_model=schemas.OrderInfoRequest)
def get_order(payload: schemas.OrderInfoRequest, db: Session = Depends(get_db)):
    order = db.query(models.Order).filter(models.Order.id == payload.order_num).first()
    order_list = db.query(models.OrderItem).filter(models.OrderItem.order_id == payload.order_num).all()

    for item in order_list:
        item.is_cancel = True

    order.is_cancel = True
    db.commit()

    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    return schemas.OrderInfoRequest(
        session_id = "",
        order_num = order.id
    )


# 계정 설정에서 얻는 주문목록
@router.get("/OrderList", response_model=schemas.OrderListResponse)
def get_order(payload: schemas.OrderListRequest, db: Session = Depends(get_db)):

    user = check_session(db, payload.session_id)

    order = db.query(models.Order).filter(models.Order.user_id == user.id).all()

    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    return [
        schemas.OrderList(
            order_num = order.id,
            order_date = order.created_at,
            price = order.total_price
        )
    ]


# 계정 설정 안에 상세 주문 목록에서 얻는 주문 정보
@router.get("/GetOrderedInfo", response_model=schemas.OrderCreate)
def get_orders(payload: schemas.OrderInfoRequest, db: Session = Depends(get_db)):
    order_info = db.query(models.Order).filter(models.Order.id == payload.order_num).first()
    order_list = db.query(models.OrderItem).filter(models.OrderItem.order_id == payload.order_num).all()

    order_list = [
        schemas.OrderItemCreate(
            product_id = item.product_id,
            quantity = item.quantity,
            price = item.price
        )
        for item in order_list
    ]

    return schemas.OrderCreate(
        session_id = "",
        address = order_info.address,
        phone = order_info.phone,
        email = order_info.email,
        total_price = order_info.total_price,
        items = order_list
    )
