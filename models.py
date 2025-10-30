from sqlalchemy.sql import func
from database import Base
from datetime import datetime
from sqlalchemy.orm import relationship
from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Boolean

# -------------------- Product --------------------
class Product(Base):
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String)
    price = Column(Float, nullable=False)
    image_url = Column(String)
    category = Column(String, nullable=True)
    product_review = Column(String, nullable=True)
    img_info = Column(String, nullable=True)

    is_big_sale = Column(Boolean, default=False)
    is_today_sale = Column(Boolean, default=False)
    is_new = Column(Boolean, default=False)

    in_basket = relationship("Basket", back_populates="product")


# -------------------- Order --------------------
class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)
    total_price = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    items = relationship("OrderItem", back_populates="order")


class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    product_id = Column(Integer, nullable=False)
    quantity = Column(Integer, nullable=False)
    price = Column(Float, nullable=False)

    order = relationship("Order", back_populates="items")


# -------------------- User --------------------
class UserProfile(Base):
    __tablename__ = "user_profiles"

    id = Column(Integer, primary_key=True, index=True)
    session_id = Column(String, unique=True, nullable=True)  # Set to nullable for sign - up
    email = Column(String, unique=True, nullable=False)
    password = Column(String, nullable=False)
    phone = Column(String, nullable=False)
    username = Column(String, unique=True)

    basket_items = relationship("Basket", back_populates="user")


# -------------------- Basket --------------------
class Basket(Base):
    __tablename__ = "baskets"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user_profiles.id"))
    product_id = Column(Integer, ForeignKey("products.id"))
    quantity = Column(Integer, default=1)
    date_time = Column(DateTime)

    user = relationship("UserProfile", back_populates="basket_items")
    product = relationship("Product", back_populates="in_basket")


# -------------------- Home data --------------------
class PopularItem(Base):
    __tablename__ = "popular_items"

    id = Column(Integer, primary_key=True)
    name = Column(String)
    description = Column(String)
    price = Column(Float)
    image_url = Column(String)
    image_info = Column(String)
    category = Column(String)


class BigSaleItem(Base):
    __tablename__ = "big_sale_items"

    id = Column(Integer, primary_key=True)
    name = Column(String)
    description = Column(String)
    price = Column(Float)
    image_url = Column(String)
    image_info = Column(String)
    category = Column(String)

class TodaySaleItem(Base):
    __tablename__ = "today_sale_items"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String, nullable=False)
    price = Column(Float, nullable=False)
    image_url = Column(String)
    image_info = Column(String)
    category = Column(String)

class NewItem(Base):
    __tablename__ = "new_items"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String, nullable=False)
    price = Column(Float, nullable=False)
    image_url = Column(String)
    image_info = Column(String)
    category = Column(String)
