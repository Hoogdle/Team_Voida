from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
from utils.session_check import *
import bcrypt


router = APIRouter(prefix="", tags=["Payment"])

# 장바구니에 결제 시도시 상품 정보를 제공하는 함수
@router.post("/BasketPayment", response_model=schemas.PaymentResponse)
def basket_payment(payload: schemas.BasketPayment, db: Session = Depends(get_db)):

    # 세션 아이디를 통해 유저 정보확인
    user = check_session(db,payload.session_id)
    basket_items = db.query(models.BasketItem).filter(models.BasketItem.user_id == user.id).all()
    basket_items.sort(key = lambda x : x.created_at, reverse = True)

    if not basket_items:
        raise HTTPException(status_code=404, detail="No items in basket")

    items = [
        schemas.BasketItem(
            product_id=item.product.id,
            img=item.product.img,
            name=item.product.title,
            price=item.product.price,
            number=item.number
        )
        for item in basket_items
    ]

    # TODO, apply user info
    return schemas.PaymentResponse(
        address= "서울특별시 서대문구 북아현로 12",
        phone= "010-1234-5678",
        email= "xodud7737@gmail.com",
        item=items
    )

# 상품 정보 페이지에서 결제요청시 사용되는 함수
@router.post("/OneItemPayment", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

    user = check_session(db,payload.session_id)

    if not user:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(payload.product_id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.img,
            name=prod.title,
            price=prod.price,
            number=1
        )]
    )

@router.post("/CardAdd", response_model=list[schemas.CardInfo])
def card_register(payload: schemas.CardRegisterRequest, db: Session = Depends(get_db)):

    user = check_session(db,payload.session_id)

    if not user:
        raise HTTPException(status_code=404, detail="User profile not found")

    card = models.Card(
        user_id = user.id,
        company = payload.card_company,
        card_code = payload.card_code,
        card_cvv = payload.card_cvv,
        date = payload.card_date,
    )

    db.add(card)
    db.commit()

    cards = db.query(models.Card).filter(models.Card.user_id == user.id).all()

    if not cards:
        raise HTTPException(status_code=404, detail="No Card Exists")


    return [
            schemas.CardInfo(
                    card_id = item.id,
                    company = item.company,
                    card_code = item.card_code,
                    date = item.date,
                    card_num = len(cards)
                )
            for item in cards
        ]


@router.post("/CardDel", response_model=list[schemas.CardInfo])
def card_del(payload: schemas.CardDeleteRequest, db: Session = Depends(get_db)):

    user = check_session(db,payload.session_id)

    if not user:
        raise HTTPException(status_code=404, detail="User profile not found")

    card = db.query(models.Card).filter(models.Card.id == payload.card_id).first()

    if not card:
        raise HTTPException(status_code=404, detail="Card Not Exits")

    db.delete(card)
    db.commit()

    cards = db.query(models.Card).filter(models.Card.user_id == user.id).all()

    if not cards:
        raise HTTPException(status_code=404, detail="Card Not Exits")

    #TODO, card_code Decode

    return [
            schemas.CardInfo(
                    card_id = item.id,
                    company = item.company,
                    card_code = item.card_code,
                    date = item.date,
                    card_num = len(cards)
                )
            for item in cards
        ]


@router.post("/CardPage", response_model=list[schemas.CardInfo])
def card_page(payload: schemas.CancelAIRequest, db: Session = Depends(get_db)):

    user = check_session(db,payload.session_id)

    if not user:
        raise HTTPException(status_code=404, detail="User profile not found")

    print(user.id)
    card_list = db.query(models.Card).filter(models.Card.user_id == user.id).all()
    print(card_list)

    if not card_list:
        return [
            schemas.CardInfo(
                card_id = -1,
                company = "",
                card_code = "",
                date = "",
                card_num = 0
            )
        ]

    #TODO, card_code Decode
    return [
        schemas.CardInfo(
            card_id = card.id,
            company = card.company,
            card_code = card.card_code,
            date = card.date,
            card_num = len(card_list)
        )
        for card in card_list
    ]


# TODO, 페이 API 구현

'''
# 단일 상품 중 인기상품 결제시 사용되는 함수
# BigSale, New... 모두 같은 로직 사용
# DB를 초기 다소 복잡하게 설계하여 로직 또한 약간 꼬이는 문제발생
@router.post("/OneItemPayment/Popular", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

    print(f"Popular index : {payload.product_id}")
    user = check_session(db,payload.session_id)

    product = db.query(models.PopularItem).filter(models.PopularItem.id == payload.product_id).first()

    re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

    profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(re_product.id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )



@router.post("/OneItemPayment/BigSale", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

    print(f"BigSale index : {payload.product_id}")
    user = check_session(db,payload.session_id)

    product = db.query(models.BigSaleItem).filter(models.BigSaleItem.id == payload.product_id).first()

    re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

    profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(re_product.id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )



@router.post("/OneItemPayment/TodaySale", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

    print(f"TodaySale index : {payload.product_id}")
    user = check_session(db,payload.session_id)

    product = db.query(models.TodaySaleItem).filter(models.TodaySaleItem.id == payload.product_id).first()

    re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

    profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(re_product.id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )


@router.post("/OneItemPayment/New", response_model=schemas.OneItemPaymentResponse)
def one_item_payment(payload: schemas.OneItemRequest, db: Session = Depends(get_db)):

    user = check_session(db,payload.session_id)

    product = db.query(models.NewItem).filter(models.NewItem.id == payload.product_id).first()

    re_product = db.query(models.Product).filter(models.Product.name == product.name).first()

    profile = db.query(models.UserProfile).filter(models.UserProfile.id == user.id).first()

    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    prod = db.query(models.Product).get(re_product.id)
    if not prod:
        raise HTTPException(status_code=404, detail="Product not found")

    return schemas.OneItemPaymentResponse(
        address="서울특별시 서대문구 북아현로 12" ,
        phone="010-1234-5678" ,
        email="xodud7737@gmail.com",
        item=[schemas.BasketItem(
            product_id=prod.id,
            img=prod.image_url,
            name=prod.name,
            price=prod.price,
            number=1
        )]
    )
'''
