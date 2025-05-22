from pydantic import BaseModel
from typing import List
from datetime import datetime
from typing import Optional
# -------------------- Product --------------------

from pydantic import BaseModel

class ProductSummary(BaseModel):
    id: int
    name: str
    description: str
    price: float
    image: Optional[str]  

    class Config:
        from_attributes = True


class ProductIDRequest(BaseModel):
    product_id: int


class ProductDetail(BaseModel):
    product_id: int
    name: str
    image: Optional[str]  
    description: str
    category: str
    price: float

    class Config:
        from_attributes = True


# -------------------- Search --------------------

class SearchRequest(BaseModel):
    search: str


# -------------------- Basket --------------------

class BasketItem(BaseModel):
    product_id: int
    img: str
    name: str
    price: float
    number: int

    class Config:
        from_attributes = True


class BasketModifyRequest(BaseModel):
    session_id: str
    product_id: int


class BasketInsertRequest(BaseModel):
    session_id: str
    product_id: int


class OneItemRequest(BaseModel):
    session_id: str
    product_id: int


# -------------------- Payment --------------------

class PaymentResponse(BaseModel):
    address: str
    phone: str
    email: str
    items: List[BasketItem]


class OneItemPaymentResponse(BaseModel):
    address: str
    phone: str
    email: str
    item: BasketItem
class OrderItemCreate(BaseModel):
    product_id: int
    quantity: int
    price: float

class OrderCreate(BaseModel):
    user_id: int
    total_price: float
    created_at: datetime
    items: List[OrderItemCreate]

class OrderResponse(BaseModel):
    id: int
    user_id: int
    total_price: float
    created_at: datetime

    class Config:
        orm_mode = True