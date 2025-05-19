from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from database import Base

# Product Table
class Product(Base):
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    image_url = Column(String)
    price = Column(String)
    description = Column(String)
    category = Column(String)

# 4 Home tab tables
class PopularItem(Product):
    __tablename__ = "popular_items"
    id = Column(Integer, ForeignKey("products.id"), primary_key=True)

class BigSaleItem(Product):
    __tablename__ = "big_sale_items"
    id = Column(Integer, ForeignKey("products.id"), primary_key=True)

class TodaySaleItem(Product):
    __tablename__ = "today_sale_items"
    id = Column(Integer, ForeignKey("products.id"), primary_key=True)

class NewItem(Product):
    __tablename__ = "new_items"
    id = Column(Integer, ForeignKey("products.id"), primary_key=True)

# User Profile
class UserProfile(Base):
    __tablename__ = "user_profiles"

    id = Column(Integer, primary_key=True)
    username = Column(String, unique=True)
    email = Column(String, unique=True)
    phone = Column(String)
    address = Column(String)
    password = Column(String)
    session_id = Column(String, unique=True)

# Basket Table
class Basket(Base):
    __tablename__ = "baskets"

    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("user_profiles.id"))
    product_id = Column(Integer, ForeignKey("products.id"))
    quantity = Column(Integer, default=1)

    product = relationship("Product")
