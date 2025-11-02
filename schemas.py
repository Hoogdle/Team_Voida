from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

# -------------------- Product --------------------
class ProductSummary(BaseModel):
    id: int
    name: Optional[str]
    description: Optional[str]
    price: Optional[float]
    image_url: Optional[str] = None
    category: Optional[str] = None

    class Config:
        from_attributes = True
        orm_mode = True

class EmailRequest(BaseModel):
    email: Optional[str]

class ProductIDRequest(BaseModel):
    product_id: int
    session_id: str

class ProductDetail(BaseModel):
    product_id: int
    name: str
    image_url: Optional[str]
    price: float
    ai_info: str
    ai_review: str

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

class BasketRequest(BaseModel):
    session_id: str

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
    address: Optional[str] = None
    phone: Optional[str] = None
    email: str
    item: List[BasketItem]


class OneItemPaymentResponse(BaseModel):
    address: str
    phone: str
    email: str
    item: List[BasketItem]

class BasketPayment(BaseModel):
    session_id: str
# -------------------- Order --------------------
class OrderItemCreate(BaseModel):
    product_id: int
    quantity: int
    price: float


class OrderCreate(BaseModel):
    session_id: str
    address: str
    phone: str
    email: str
    total_price: float
    items: List[OrderItemCreate]

class OrderInfoRequest(BaseModel):
    session_id: str
    order_num: str


class OrderResponse(BaseModel):
    order_num: str
    total_price: float
    success: bool

    model_config = {
        "from_attributes": True
    }

class OrderList(BaseModel):
    order_num: str
    order_date: str # TODO, check the type
    price: float

class OrderListRequest(BaseModel):
    session_id: str

class OrderListResponse(BaseModel):
    order_list: List[OrderList]

class TodaySaleItemResponse(BaseModel):
    id: int
    name: str
    description: str
    price: float
    image_url: Optional[str] = None
    category: Optional[str] = None

    class Config:
        from_attributes = True


# -------------------- User --------------------
class SignUpRequest(BaseModel):
    email: str
    pw: str
    cell: str
    un: str


class LoginRequest(BaseModel):
    email: str
    pw: str


class LoginResponse(BaseModel):
    session_id: str


class UserNameRequest(BaseModel):
    un: str

class ReviewRequest(BaseModel):
    product_id: int
    session_id: str

class ReviewProvide(BaseModel):
    ai_review: str

class AssistantCategory(BaseModel):
    voiceInput: str

class AssistantSearch(BaseModel):
    voiceInput: str

class CancelAIRequest(BaseModel):
    session_id: str
