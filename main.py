from fastapi import FastAPI, HTTPException, Body
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker
from passlib.hash import bcrypt

# PostgreSQL 
DATABASE_URL = "postgresql://postgres:your_password@localhost:5432/postgres"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)
Base = declarative_base()

app = FastAPI()

# ---------------------- #
# 🧱 Models
# ---------------------- #
class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    description = Column(String)
    price = Column(Float)

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, nullable=False)
    password = Column(String, nullable=False)
    username = Column(String, nullable=True)

Base.metadata.create_all(bind=engine)

# ---------------------- #
# 📦 Product CRUD API
# ---------------------- #
@app.post("/products/")
def create_product(name: str, description: str, price: float):
    db = SessionLocal()
    product = Product(name=name, description=description, price=price)
    db.add(product)
    db.commit()
    db.refresh(product)
    db.close()
    return product

@app.get("/products/")
def read_products():
    db = SessionLocal()
    products = db.query(Product).all()
    db.close()
    return products

@app.get("/products/{product_id}")
def read_product(product_id: int):
    db = SessionLocal()
    product = db.query(Product).filter(Product.id == product_id).first()
    db.close()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product

@app.put("/products/{product_id}")
def update_product(product_id: int, name: str, description: str, price: float):
    db = SessionLocal()
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        db.close()
        raise HTTPException(status_code=404, detail="Product not found")
    product.name = name
    product.description = description
    product.price = price
    db.commit()
    db.refresh(product)
    db.close()
    return product

@app.delete("/products/{product_id}")
def delete_product(product_id: int):
    db = SessionLocal()
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        db.close()
        raise HTTPException(status_code=404, detail="Product not found")
    db.delete(product)
    db.commit()
    db.close()
    return {"message": "Product deleted successfully"}

# ---------------------- #
# 👤 User API
# ---------------------- #

# signup API
@app.post("/signup")
def signup(email: str = Body(...), password: str = Body(...)):
    db = SessionLocal()
    if db.query(User).filter(User.email == email).first():
        db.close()
        raise HTTPException(status_code=400, detail="Email already registered")
    hashed_pw = bcrypt.hash(password)
    user = User(email=email, password=hashed_pw)
    db.add(user)
    db.commit()
    db.refresh(user)
    db.close()
    return {"message": "Signup successful!"}

# Login API
@app.post("/login")
def login(email: str = Body(...), password: str = Body(...)):
    db = SessionLocal()
    user = db.query(User).filter(User.email == email).first()
    db.close()
    if not user or not bcrypt.verify(password, user.password):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return {"message": "Login successful!"}

# set-username API
@app.post("/set-username")
def set_username(email: str = Body(...), username: str = Body(...)):
    db = SessionLocal()
    user = db.query(User).filter(User.email == email).first()
    if not user:
        db.close()
        raise HTTPException(status_code=404, detail="User not found")
    user.username = username
    db.commit()
    db.refresh(user)
    db.close()
    return {"message": "Username updated successfully!"}
@app.get("/")
def read_root():
    return {"message": "Server is running!"}
