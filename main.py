from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import (  # Import all routers from the routers package
    home_router,
    search_router,
    product_router,
    basket_router,
    payment_router,
    order_router,
    user_router,
	assistant_router
  # Add the new user router
)
from database import engine
import models
import sys
sys.path.insert(1, "/home/xodud7737/AiApp/LLaVA-NeXT")

# ai 모델을 위한 라이브러리

# -------------------- Create Database Tables --------------------
models.Base.metadata.create_all(bind=engine)

# -------------------- Initialize FastAPI App --------------------
app = FastAPI(
    title="AI Market API",
    description="Market for visually impaired users",
    version="1.0.0",
)

# -------------------- CORS Configuration --------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Replace with specific origins if known
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------- Register All Routes --------------------
app.include_router(home_router)       # Existing home routes
app.include_router(search_router)     # Existing search routes
app.include_router(product_router)    # Existing product routes
app.include_router(basket_router)     # Existing basket routes
app.include_router(payment_router)    # Existing payment routes
app.include_router(order_router)      # Existing order routes
app.include_router(user_router)       # New: User - related routes (SignUp, SignIn, UserName)
app.include_router(assistant_router)      
# -------------------- Root Endpoint --------------------
@app.get("/")
def read_root():
    return {
        "message": "Hello, this is the AI Market API for visually impaired users.",
        "status": "running",
        "routes": [
            "/Home",
            "/SearchItems",
            "/ProductInfo",
            "/Basket",
            "/BasketAdd",
            "/BasketInsert",
            "/Order",
            "/SignUp",   # New: Added to route list
            "/SignIn",   # New: Added to route list
            "/UserName",  # New: Added to route list
			"AssistantCategory"
        ]
    }
