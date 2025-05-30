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
    user_id: int
    total_price: float
    created_at: datetime
    items: List[OrderItemCreate]


class OrderResponse(BaseModel):
    id: int
    user_id: int
    total_price: float
    created_at: datetime

    model_config = {
        "from_attributes": True
    }


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
