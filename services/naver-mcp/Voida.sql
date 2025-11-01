CREATE TABLE "products" (
  "id" SERIAL PRIMARY KEY,
  "product_url" TEXT UNIQUE,
  "img" TEXT,
  "title" TEXT,
  "price" BIGINT,
  "seller" TEXT,
  "category" TEXT,
  "description" TEXT,
  "ai_info" TEXT,
  "ai_review" TEXT,
  "created_at" TIMESTAMP DEFAULT (NOW()),
  "updated_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "images" (
  "id" SERIAL PRIMARY KEY,
  "product_id" INTEGER NOT NULL,
  "image_url" TEXT UNIQUE NOT NULL,
  "status" TEXT DEFAULT 'pending',
  "created_at" TIMESTAMP DEFAULT (NOW()),
  "updated_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "users" (
  "id" SERIAL PRIMARY KEY,
  "email" TEXT UNIQUE NOT NULL,
  "pw" TEXT NOT NULL,
  "cell" TEXT,
  "un" TEXT,
  "address" TEXT,
  "created_at" TIMESTAMP DEFAULT (NOW()),
  "updated_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "sessions" (
  "session_id" TEXT PRIMARY KEY,
  "user_id" INTEGER,
  "created_at" TIMESTAMP DEFAULT (NOW()),
  "expires_at" TIMESTAMP
);

CREATE TABLE "basket_items" (
  "id" SERIAL PRIMARY KEY,
  "user_id" TEXT NOT NULL,
  "product_id" INTEGER NOT NULL,
  "name" TEXT,
  "img" TEXT,
  "price" BIGINT,
  "number" INTEGER DEFAULT 1,
  "created_at" TIMESTAMP DEFAULT (NOW()),
  "updated_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "orders" (
  "id" SERIAL PRIMARY KEY,
  "user_id" INTEGER,
  "total_price" BIGINT,
  "address" TEXT,
  "phone" TEXT,
  "email" TEXT,
  "created_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "order_items" (
  "id" SERIAL PRIMARY KEY,
  "order_id" INTEGER NOT NULL,
  "product_id" INTEGER,
  "quantity" INTEGER NOT NULL,
  "price" BIGINT,
  "created_at" TIMESTAMP DEFAULT (NOW())
);

CREATE TABLE "card" (
  "id" SERIAL PRIMARY KEY,
  "user_id" INTEGER,
  "name" TEXT,
  "company" TEXT,
  "card_code" "CHAR(64)",
  "date" Date
);

CREATE TABLE "pay" (
  "id" SERIAL PRIMARY KEY,
  "user_id" INTEGER,
  "name" TEXT,
  "company" TEXT,
  "pay_id" TEXT
);

CREATE TABLE "Home" (
  "id" SERIAL PRIMARY KEY,
  "product_id" INTEGER,
  "sector" INTEGER
);

CREATE UNIQUE INDEX ON "basket_items" ("user_id", "product_id");

ALTER TABLE "images" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id") ON DELETE CASCADE;

ALTER TABLE "sessions" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE SET NULL;

ALTER TABLE "orders" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE SET NULL;

ALTER TABLE "order_items" ADD FOREIGN KEY ("order_id") REFERENCES "orders" ("id") ON DELETE CASCADE;

ALTER TABLE "order_items" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id") ON DELETE SET NULL;

ALTER TABLE "basket_items" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "card" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "pay" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "Home" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id");

ALTER TABLE "basket_items" ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id");
