from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from database import Base, engine
import models


from routers.user_routes import router as user_router
from routers.product_routes import router as product_router
from routers.order_routes import router as order_router
from routers.review_routes import router as review_router
from routers.cart_routes import router as cart_router



app = FastAPI()


Base.metadata.create_all(bind=engine)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(user_router)
app.include_router(product_router)
app.include_router(order_router)
app.include_router(review_router)  
app.include_router(cart_router)