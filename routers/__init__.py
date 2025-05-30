from .basket_routes import router as basket_router
from .home_routes import router as home_router
from .order_routes import router as order_router
from .payment_routes import router as payment_router
from .product_routes import router as product_router
from .search_routes import router as search_router
from .user_routes import router as user_router

__all__ = [
    "basket_router",
    "home_router",
    "order_router",
    "payment_router",
    "product_router",
    "search_router",
    "user_router"
]