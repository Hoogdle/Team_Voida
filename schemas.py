from pydantic import BaseModel, EmailStr
from typing import Optional

class SignupRequest(BaseModel):
    email: EmailStr
    password: str

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class TokenRequest(BaseModel):
    token: str

class UsernameRequest(BaseModel):
    username: str

class ProductCreate(BaseModel):
    name: str
    description: str
    category: str
    price: float
    product_image: str
    product_review: str

class ProductOut(ProductCreate):
    id: int

    class Config:
        from_attributes = True  

class PurchaseRequest(TokenRequest):
    product_id: int

class ReviewCreate(BaseModel):
    token: str
    product_id: int
    rating: int
    comment: str

class CartAddRequest(BaseModel):
    token: str
    product_id: int
    quantity: int

class OrderCreateRequest(BaseModel):
    token: str
