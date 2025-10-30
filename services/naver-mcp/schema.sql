-- Products (상품)
CREATE TABLE IF NOT EXISTS products (
  id SERIAL PRIMARY KEY,
  product_url TEXT UNIQUE,
  title TEXT,
  price BIGINT,
  seller TEXT,
  category TEXT,
  description TEXT,
  ai_info TEXT,
  ai_review TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Images (이미지)
CREATE TABLE IF NOT EXISTS images (
  id SERIAL PRIMARY KEY,
  product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url TEXT UNIQUE NOT NULL,
  status TEXT DEFAULT 'pending',
  sha256 CHAR(64),
  bytes INTEGER,
  width INTEGER,
  height INTEGER,
  saved_path TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
 
-- Images 인덱스
CREATE INDEX IF NOT EXISTS idx_images_product_id ON images(product_id);
CREATE INDEX IF NOT EXISTS idx_images_status ON images(status);
-- 고유 인덱스 (서비스 스키마와 동일 명명)
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_product_url ON products(product_url);
CREATE UNIQUE INDEX IF NOT EXISTS ux_images_image_url ON images(image_url);

-- Users (회원)
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  pw TEXT NOT NULL,
  cell TEXT,
  un TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Sessions (세션)
CREATE TABLE IF NOT EXISTS sessions (
  session_id TEXT PRIMARY KEY,
  user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);

-- Basket (장바구니: 세션 기반)
CREATE TABLE IF NOT EXISTS basket_items (
  id SERIAL PRIMARY KEY,
  session_id TEXT NOT NULL,
  product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  name TEXT,
  img TEXT,
  price BIGINT,
  number INTEGER DEFAULT 1,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(session_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_basket_session ON basket_items(session_id);
CREATE INDEX IF NOT EXISTS idx_basket_product ON basket_items(product_id);

-- Orders (주문 헤더)
CREATE TABLE IF NOT EXISTS orders (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
  total_price BIGINT,
  address TEXT,
  phone TEXT,
  email TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);

-- Order Items (주문 상세)
CREATE TABLE IF NOT EXISTS order_items (
  id SERIAL PRIMARY KEY,
  order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,
  quantity INTEGER NOT NULL,
  price BIGINT,
  created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
