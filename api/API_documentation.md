Backend API Documentation
1. Get Products

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app
Description: Retrieves a list of products with their details for the Shop page.
Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "productId": "String",
    "img": "String (URL)",
    "rank": "String",
    "name": "String",
    "price": "String",
    "discount": "String"
  },
  ...
]





2. User Login

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/login
Description: Authenticates a user with their credentials.
Request:
Content-Type: application/json
Body:{
  "id": "String",
  "pw": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body: {
  "message": "Login successful",
  "token": "String (JWT token)"
}





3. Update Username

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/username
Description: Updates the username of the authenticated user.
Request:
Content-Type: application/json
Body:{
  "un": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body: {
  "message": "Username updated successfully"
}





4. Search Products

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/search
Description: Searches for products based on a query string.
Query Parameters:
q: "String" (search query, e.g., "Fanta")


Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "productId": "String",
    "img": "String (URL)",
    "name": "String",
    "price": "String",
    "discount": "String"
  },
  ...
]





5. Filter Products

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/filter
Description: Filters products based on categories, price range, and other criteria.
Query Parameters:
category: "String" (e.g., "Clothing", "Drinks")
priceMin: "Number" (e.g., 10)
priceMax: "Number" (e.g., 50)
color: "String" (e.g., "Red")


Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "productId": "String",
    "img": "String (URL)",
    "name": "String",
    "price": "String",
    "discount": "String"
  },
  ...
]





6. Get Product Details

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/product/{productId}
Description: Retrieves detailed information about a specific product.
Path Parameters:
productId: "String"


Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "productId": "String",
  "img": "String (URL)",
  "name": "String",
  "price": "String",
  "discount": "String",
  "description": "String",
  "material": "String",
  "origin": "String",
  "sizeOptions": ["String"],
  "colorOptions": ["String"]
}





7. Get Product Reviews

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/product/{productId}/reviews
Description: Retrieves reviews for a specific product.
Path Parameters:
productId: "String"


Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "reviewer": "String",
    "rating": "Number",
    "comment": "String",
    "date": "String"
  },
  ...
]





8. Add Product Review

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/product/{productId}/reviews
Description: Adds a review for a specific product.
Path Parameters:
productId: "String"


Request:
Content-Type: application/json
Body:{
  "rating": "Number",
  "comment": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "message": "Review added successfully"
}





9. Add to Cart

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/cart
Description: Adds a product to the user's cart. The backend retrieves the product price using the provided productId.
Request:
Content-Type: application/json
Body:{
  "productId": "String",
  "quantity": "Number",
  "size": "String",
  "color": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "message": "Product added to cart",
  "cartId": "String",
  "product": {
    "productId": "String",
    "name": "String",
    "price": "String",
    "quantity": "Number",
    "size": "String",
    "color": "String",
    "totalPrice": "String"
  }
}





10. Get Cart

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/cart
Description: Retrieves the user's cart contents.
Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "cartId": "String",
  "items": [
    {
      "productId": "String",
      "name": "String",
      "img": "String (URL)",
      "price": "String",
      "quantity": "Number",
      "size": "String",
      "color": "String",
      "totalPrice": "String"
    },
    ...
  ],
  "totalAmount": "String"
}





11. Update Cart Item

Method: PUT
URL: https://fluent-marmoset-immensely.ngrok-free.app/cart/{productId}
Description: Updates the quantity, size, or color of a product in the cart.
Path Parameters:
productId: "String"


Request:
Content-Type: application/json
Body:{
  "quantity": "Number",
  "size": "String",
  "color": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "message": "Cart updated successfully",
  "totalAmount": "String"
}





12. Remove from Cart

Method: DELETE
URL: https://fluent-marmoset-immensely.ngrok-free.app/cart/{productId}
Description: Removes a product from the user's cart.
Path Parameters:
productId: "String"


Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "message": "Product removed from cart",
  "totalAmount": "String"
}





13. Apply Voucher

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/voucher
Description: Applies a voucher code to the user's cart.
Request:
Content-Type: application/json
Body:{
  "voucherCode": "String"
}




Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "message": "Voucher applied",
  "discount": "String",
  "newTotal": "String"
}





14. Get Categories

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/categories
Description: Retrieves the list of product categories.
Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "categoryId": "String",
    "name": "String"
  },
  ...
]





15. Get Size Guide

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app/size-guide
Description: Retrieves size guide information for products.
Response:
Status: 200 OK
Content-Type: application/json
Body:{
  "category": "String",
  "sizes": [
    {
      "size": "String",
      "measurements": {
        "length": "String",
        "width": "String",
        "height": "String"
      }
    },
    ...
  ]
}





