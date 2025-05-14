# 🛒 AI Market Backend API

This is the backend API for an assistive e-commerce application designed for **visually impaired users**.  
Built with **FastAPI** + **PostgreSQL**.

## 📦 Features

- 🧾 **Signup/Login** (JWT-based auth)
- 🙋‍♂️ Set username
- 📦 Create and list products
- 🔍 Search product by keyword
- 🛍️ Purchase product
- 🛒 Add to cart
- 📦 Create order
- 📑 View order history
- ⭐ Leave product reviews

## 🛠️ Tech Stack

- Python 3.10
- FastAPI
- SQLAlchemy
- PostgreSQL
- JWT (python-jose)
- Pydantic
- Uvicorn

## 📂 API Endpoints

| Method | Endpoint             | Description               |
|--------|----------------------|---------------------------|
| POST   | `/signup`            | Register a new user       |
| POST   | `/login`             | User login, returns token |
| POST   | `/set-username`      | Set or update username    |
| GET    | `/products/`         | List all products         |
| GET    | `/products/search`   | Search by keyword         |
| GET    | `/products/{id}`     | Get product by ID         |
| POST   | `/products/`         | Add a new product         |
| POST   | `/purchase`          | Purchase product          |
| POST   | `/order`             | Create order              |
| GET    | `/order-history`     | View order history        |
| POST   | `/review`            | Leave a review            |
| POST   | `/cart/add`          | Add item to cart          |

## 🔐 Authentication

All secure endpoints require **JWT token** in request body or header.

```json
{
  "token": "your_jwt_token"
}



🚀 How to Run Locally
# 1. Clone repository
git clone https://github.com/your/repo.git
cd ai_project

# 2. Install dependencies
pip install -r requirements.txt

# 3. Setup PostgreSQL and .env
# Edit `.env` file
DATABASE_URL=postgresql://postgres:yourpassword@localhost:5432/postgres
JWT_SECRET=your_jwt_secret_key

# 4. Run server
uvicorn main:app --reload
