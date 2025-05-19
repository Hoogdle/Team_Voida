# 🛍️ AI-Powered Market App for Visually Impaired

This is the **backend API** for a market-style application designed to help people with **visual disabilities** easily browse and purchase products.

The application provides **product information with image descriptions**, **shopping basket management**, and **simple payments**.

---

## 🔧 Tech Stack

- **FastAPI** (Python 3.10)
- **PostgreSQL** for relational database
- **SQLAlchemy** as ORM
- **Pydantic v2** for data validation
- **Docker** (optional)
- **Postman** for API testing

---

## 🚀 API Endpoints

### 📦 Product

- `POST /ProductInfo` — Get full detail of a product
- `POST /CategoriesItems/{category}` — Get items by category
- `POST /SearchItems` — Search products by keyword

### 🛒 Basket

- `POST /Basket?session_id=xxx` — Get current session's basket
- `POST /BasketAdd?session_id=xxx` — Add item to basket
- `POST /BasketSub?session_id=xxx` — Decrease item quantity
- `POST /BasketDel?session_id=xxx` — Remove item
- `POST /BasketInsert?session_id=xxx` — Add item with specified count

### 💳 Payment

- `POST /BasketPayment?session_id=xxx` — Checkout all items
- `POST /OneItemPayment?session_id=xxx` — Checkout a single item

### 🏠 Home Page APIs

- `POST /Home` — Get all homepage items
- `POST /PopularItems`, `/BigSaleItems`, `/TodaySaleItems`, `/NewItems` — Get curated sets of products

---

## ⚙️ How to Run Locally

```bash
# 1. Clone the repo
git clone https://github.com/your-username/ai_project.git
cd ai_project

# 2. Create virtual environment and install dependencies
python3 -m venv env
source env/bin/activate
pip install -r requirements.txt

# 3. Set up PostgreSQL & .env file
# Example .env:
# DATABASE_URL=postgresql://postgres:your_password@localhost:5432/postgres

# 4. Run server
uvicorn main:app --reload
