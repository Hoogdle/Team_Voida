Backend API Documentation
1. Get Products

Method: GET
URL: https://fluent-marmoset-immensely.ngrok-free.app
Description: Retrieves a list of products with their details.
Response:
Status: 200 OK
Content-Type: application/json
Body:[
  {
    "img": "String (URL)",
    "rank": "String",
    "name": "String",
    "price": "String",
    "discount": "String"
  },
  ...
]




Error Handling:
404 Not Found: If no products are available.
500 Internal Server Error: If there is a server-side issue.



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
Status: 200 OK (on success)
Content-Type: application/json
Body: {
  "message": "Login successful",
  "token": "String (JWT token)"
}


Status: 401 Unauthorized (on failure)
Body: {
  "message": "Invalid credentials"
}




Error Handling:
400 Bad Request: If request body is malformed.
500 Internal Server Error: If there is a server-side issue.



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
Status: 200 OK (on success)
Content-Type: application/json
Body: {
  "message": "Username updated successfully"
}


Status: 400 Bad Request (on failure)
Body: {
  "message": "Invalid username"
}




Error Handling:
401 Unauthorized: If authentication token is missing or invalid.
500 Internal Server Error: If there is a server-side issue.



4. Add to Cart

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/cart
Description: Adds a product to the user's cart.
Request:
Content-Type: application/json
Body:{
  "productId": "String",
  "quantity": "Number"
}




Response:
Status: 200 OK (on success)
Content-Type: application/json
Body: {
  "message": "Product added to cart",
  "cartId": "String"
}




Error Handling:
400 Bad Request: If productId or quantity is invalid.
401 Unauthorized: If user is not authenticated.
500 Internal Server Error: If there is a server-side issue.



5. Apply Voucher

Method: POST
URL: https://fluent-marmoset-immensely.ngrok-free.app/voucher
Description: Applies a voucher code to the user's cart.
Request:
Content-Type: application/json
Body:{
  "voucherCode": "String"
}




Response:
Status: 200 OK (on success)
Content-Type: application/json
Body: {
  "message": "Voucher applied",
  "discount": "String"
}


Status: 400 Bad Request (on failure)
Body: {
  "message": "Invalid voucher code"
}




Error Handling:
401 Unauthorized: If user is not authenticated.
500 Internal Server Error: If there is a server-side issue.



