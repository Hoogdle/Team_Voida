
from datetime import datetime, date
from sqlalchemy import (
    Column, Integer, String, Text, BigInteger, DateTime, Date, ForeignKey, Boolean
)
from sqlalchemy.orm import relationship, declarative_base

Base = declarative_base()


# --------------------------
# PRODUCTS
# --------------------------
class Product(Base):
    __tablename__ = "products"

    id = Column(Integer, primary_key=True, index=True)
    product_url = Column(Text, unique=True)
    img = Column(Text)
    title = Column(Text)
    price = Column(BigInteger)
    seller = Column(Text)
    category = Column(Text)
    description = Column(Text)
    ai_info = Column(Text)
    ai_review = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    images = relationship("Image", back_populates="product", cascade="all, delete-orphan")
    basket_items = relationship("BasketItem", back_populates="product", cascade="all, delete-orphan")
    order_items = relationship("OrderItem", back_populates="product")
    home = relationship("Home", back_populates="product", uselist=False)
    review = relationship("Review", back_populates="product", uselist=False)


# --------------------------
# IMAGES
# --------------------------
class Image(Base):
    __tablename__ = "images"

    id = Column(Integer, primary_key=True, index=True)
    product_id = Column(Integer, ForeignKey("products.id", ondelete="CASCADE"), nullable=False)
    image_url = Column(Text, unique=True, nullable=False)
    status = Column(Text, default="pending")
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow)

    product = relationship("Product", back_populates="images")


# --------------------------
# USERS
# --------------------------
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(Text, unique=True, nullable=False)
    pw = Column(Text, nullable=False)
    cell = Column(Text)
    un = Column(Text)
    address = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow)

    sessions = relationship("Session", back_populates="user", cascade="all, delete-orphan")
    basket_items = relationship("BasketItem", back_populates="user")
    orders = relationship("Order", back_populates="user")
    cards = relationship("Card", back_populates="user")
    pays = relationship("Pay", back_populates="user")


# --------------------------
# SESSIONS
# --------------------------
class Session(Base):
    __tablename__ = "sessions"

    session_id = Column(Text, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"))
    created_at = Column(DateTime, default=datetime.utcnow)
    expires_at = Column(DateTime)

    user = relationship("User", back_populates="sessions")


# --------------------------
# BASKET ITEMS
# --------------------------
class BasketItem(Base):
    __tablename__ = "basket_items"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    product_id = Column(Integer, ForeignKey("products.id"), nullable=False)
    # name = Column(Text)
    # img = Column(Text)
    # price = Column(BigInteger)
    number = Column(Integer, default=1)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="basket_items")
    product = relationship("Product", back_populates="basket_items")


# --------------------------
# ORDERS
# --------------------------
class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"))
    total_price = Column(BigInteger)
    address = Column(Text)
    phone = Column(Text)
    email = Column(Text)
    is_cancel = Column(Boolean)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="orders")
    order_items = relationship("OrderItem", back_populates="order", cascade="all, delete-orphan")


# --------------------------
# ORDER ITEMS
# --------------------------
class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False)
    product_id = Column(Integer, ForeignKey("products.id", ondelete="SET NULL"))
    quantity = Column(Integer, nullable=False)
    price = Column(BigInteger)
    is_cancel = Column(Boolean)
    created_at = Column(DateTime, default=datetime.utcnow)

    order = relationship("Order", back_populates="order_items")
    product = relationship("Product", back_populates="order_items")


# --------------------------
# CARD
# --------------------------
class Card(Base):
    __tablename__ = "card"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    company = Column(Text)
    card_code = Column(Text)
    card_cvv =Column(Text)
    date = Column(Text)

    user = relationship("User", back_populates="cards")


# --------------------------
# PAY
# --------------------------
class Pay(Base):
    __tablename__ = "pay"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    name = Column(Text)
    company = Column(Text)
    pay_id = Column(Text)

    user = relationship("User", back_populates="pays")


# --------------------------
# HOME
# --------------------------
class Home(Base):
    __tablename__ = "home"

    id = Column(Integer, primary_key=True, index=True)
    product_id = Column(Integer, ForeignKey("products.id"))
    sector = Column(Integer)

    product = relationship("Product", back_populates="home")


# --------------------------
# REVIEW
# --------------------------
class Review(Base):
    __tablename__ = "review"

    id = Column(Integer, primary_key=True, index=True)
    product_id = Column(Integer, ForeignKey("products.id"))
    review1 = Column(Text)
    review2 = Column(Text)
    review3 = Column(Text)

    product = relationship("Product", back_populates="review")

