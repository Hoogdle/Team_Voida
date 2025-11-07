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
        card_id=order.card_id,
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
            quantity=item.number,
            price=item.price,
            is_cancel = False
        )
        db.add(db_item)
    db.commit()

    
    return schemas.OrderResponse(
        order_num = str(db_order.id),
        address = order.address,
        email = order.email,
        cell = order.phone,
        item = [
            schemas.BasketItem(
                product_id = item.product_id,
                img = item.img,
                name = item.name,
                price = item.price,
                number = item.number
            )
            for item in order.items
            ]
    )



@router.post("/CancelOrder", response_model=bool)
def get_order(payload: schemas.OrderInfoRequest, db: Session = Depends(get_db)):
    order = db.query(models.Order).filter(models.Order.id == payload.order_num).first()
    order_list = db.query(models.OrderItem).filter(models.OrderItem.order_id == payload.order_num).all()

    for item in order_list:
        item.is_cancel = True

    order.is_cancel = True
    db.commit()

    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
  
    return True
 

# 계정 설정에서 얻는 주문목록
@router.post("/PayHistoryList", response_model=list[schemas.PayHistoryList])
def get_order(payload: schemas.OrderListRequest, db: Session = Depends(get_db)):

    user = check_session(db, payload.session_id)
    cards = db.query(models.Card).filter(models.Card.user_id == user.id).all()
    orders = [(card.id, card.card_code, card.date, card.company, len(cards), db.query(models.Order).filter(models.Order.user_id == user.id, models.Order.card_id == card.id)) for card in cards]
	# (card_id, card_code, card_date, card_company, card_num, orders)


    if not orders:
        raise HTTPException(status_code=404, detail="Order not found")

    return [
       schemas.PayHistoryList(
			card_id = order[0],
			card_code = order[1],
			card_date = order[2],
			card_company = order[3],
			card_num = order[4],
			pay_list = [
				schemas.PayHistory(
					is_refund = inner_order.is_cancel,
					date = inner_order.created_at,
					order_num = str(inner_order.id),
					price = int(inner_order.total_price)
				)
				for inner_order in order[5]
			]
		)
		for order in orders
    ]


# 계정 설정 안에 상세 주문 목록에서 얻는 주문 정보
@router.post("/PayDetailHistoryList", response_model=schemas.OrderDetailHistory)
def get_orders(payload: schemas.OrderInfoRequest, db: Session = Depends(get_db)):
    order_info = db.query(models.Order).filter(models.Order.id == payload.order_num).first()
    order_list = db.query(models.OrderItem).filter(models.OrderItem.order_id == payload.order_num).all()
 
    return schemas.OrderDetailHistory(
        order_num = str(order_info.id),
        address = order_info.address,
        cell = order_info.phone,
        email = order_info.email,
        total_price = order_info.total_price,
        items = [
                schemas.BasketItem(
                    product_id = item.product_id,
                    img = item.product.img,
                    name = item.product.title,
                    price = item.product.price,
                    number = item.quantity
            )
            for item in order_list
        ]
    )
