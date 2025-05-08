## 📄 API Documentation: Shopping App

### 1. 🔍 Get All Categories

**Method:** `GET`
**URL:** `/categories`
**Response:**

```json
[
  { "id": 1, "name": "Clothing", "icon": "url" },
  { "id": 2, "name": "Shoes", "icon": "url" },
  { "id": 3, "name": "Drinks", "icon": "url" },
  { "id": 4, "name": "Fruits", "icon": "url" }
]
```

### 2. 🔍 Get Products (Shop page, Flash Sale, New Items)

**Method:** `GET`
**URL:** `/products`
**Query Params (optional):**

* `category` (string)
* `flashSale` (boolean)
* `newItems` (boolean)

**Response:**

```json
[
  {
    "id": 101,
    "name": "Coca Cola",
    "price": "24.00",
    "discount": "10%",
    "img": "url",
    "category": "Drinks"
  }
]
```

### 3. 🔍 Product Detail

**Method:** `GET`
**URL:** `/products/:id`
**Response:**

```json
{
  "id": 101,
  "name": "Coca Cola",
  "description": "Lorem ipsum...",
  "price": "24.00",
  "discount": "10%",
  "colors": ["Red", "Green"],
  "sizes": ["S", "M", "L"],
  "images": ["url1", "url2"],
  "specs": {
    "material": "Cotton",
    "origin": "Korea"
  },
  "delivery": [
    { "type": "Standard", "price": "3.00", "time": "5 day" },
    { "type": "Express", "price": "5.00", "time": "1 day" }
  ],
  "rating": 4.5,
  "reviews": [
    { "user": "Veronika", "rating": 5, "comment": "Very good!" }
  ]
}
```

### 4. 🔍 Search Products

**Method:** `GET`
**URL:** `/search`
**Query Params:** `q=banana`
**Response:** same as `/products`

### 5. 🧾 Filter Products

**Method:** `POST`
**URL:** `/products/filter`
**Body:**

```json
{
  "size": "M",
  "color": "Red",
  "priceRange": [10, 50],
  "sort": "priceLowToHigh"
}
```

**Response:** same as `/products`

### 6. 🛒 View Cart

**Method:** `GET`
**URL:** `/cart`
**Response:**

```json
[
  { "productId": 101, "name": "Red Dress", "qty": 2, "price": "17.00", "img": "url" }
]
```

### 7. ➕ Add to Cart

**Method:** `POST`
**URL:** `/cart`
**Body:**

```json
{ "productId": 101, "qty": 2 }
```

### 8. ❌ Remove from Cart

**Method:** `DELETE`
**URL:** `/cart/:productId`

### 9. 💳 Checkout

**Method:** `POST`
**URL:** `/checkout`
**Body:**

```json
{
  "shippingAddress": "Somewhere in Korea",
  "contactInfo": { "name": "User", "phone": "010-xxxx" },
  "items": [
    { "productId": 101, "qty": 2 }
  ],
  "paymentMethod": "Card"
}
```

**Response:**

```json
{ "status": "success", "message": "Order placed successfully" }
```

### 10. ⭐ Submit Review

**Method:** `POST`
**URL:** `/reviews`
**Body:**

```json
{ "productId": 101, "rating": 5, "comment": "Excellent!" }
```

### 11. 👤 Login

**Method:** `POST`
**URL:** `/login`
**Body:**

```json
{ "id": "user123", "pw": "password" }
```

### 12. 🔄 Set Username

**Method:** `POST`
**URL:** `/username`
**Body:**

```json
{ "un": "NewUsername" }
```

---
