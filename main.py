from fastapi import FastAPI
from routers import (
    home_routes,
    search_routes,
    product_routes,
    basket_routes,
    payment_routes,
)
from database import engine
import models

# DB table-уудыг автоматаар үүсгэнэ
models.Base.metadata.create_all(bind=engine)

# App
app = FastAPI()

# Routes
app.include_router(home_routes.router)
app.include_router(search_routes.router)
app.include_router(product_routes.router)
app.include_router(basket_routes.router)
app.include_router(payment_routes.router)

@app.get("/")
def read_root():
    return {"message": "Hello, this is the AI Market API for visually impaired users."}
