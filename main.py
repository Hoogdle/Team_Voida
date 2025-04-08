from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel
from motor.motor_asyncio import AsyncIOMotorClient
from bson import ObjectId
from datetime import datetime
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
# MongoDB-тэй холбох
MONGO_URL = "mongodb://localhost:27017"
client = AsyncIOMotorClient(MONGO_URL)
db = client["ai_project_db"]
collection = db["products"]


# Pydantic загвар
class Product(BaseModel):
    name: str
    price: float


# 🔹 1. Бүх бүтээгдэхүүн авах (pagination нэмсэн)
@app.get("/products/")
async def get_products(skip: int = 0, limit: int = 10):
    products_cursor = collection.find().skip(skip).limit(limit)
    products = await products_cursor.to_list(length=limit)
    
    for product in products:
        product["_id"] = str(product["_id"])
    
    return {"products": products}


# 🔹 2. ID-аар бүтээгдэхүүн авах
@app.get("/products/{product_id}")
async def get_product_by_id(product_id: str):
    product = await collection.find_one({"_id": ObjectId(product_id)})
    if not product:
        raise HTTPException(status_code=404, detail="Бүтээгдэхүүн олдсонгүй")
    
    product["_id"] = str(product["_id"])
    return product


# 🔹 3. Нэрээр хайлт хийх
@app.get("/products/search/")
async def search_products(name: str = Query(..., description="Хайх бүтээгдэхүүний нэр")):
    products_cursor = collection.find({"name": {"$regex": name, "$options": "i"}})
    products = await products_cursor.to_list(length=100)
    
    for product in products:
        product["_id"] = str(product["_id"])
    
    return {"products": products}


# 🔹 4. Худалдан авалтыг бүртгэх
@app.post("/purchase/")
async def record_purchase(user_id: str, product_id: str):
    purchase = {
        "user_id": user_id,
        "product_id": product_id,
        "timestamp": datetime.utcnow()
    }
    result = await db["purchases"].insert_one(purchase)
    
    return {
        "message": "Худалдан авалт бүртгэгдлээ!",
        "purchase_id": str(result.inserted_id)
    }
