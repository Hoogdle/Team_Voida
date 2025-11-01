-- =========================
-- Core Tables
-- =========================

-- Products (상품) : 크롤러 레코드 키에 맞춰 컬럼 추가
CREATE TABLE IF NOT EXISTS products (
  id           SERIAL PRIMARY KEY,
  -- 수집 메타
  keyword      TEXT,           -- 검색 키워드
  sort         TEXT,           -- 정렬 옵션(예: price_asc 등)
  page_start   INTEGER,        -- 페이지 시작 인덱스(크롤링 오프셋)
  -- 본문 데이터
  title        TEXT,
  description  TEXT,
  seller       TEXT,
  category     TEXT,
  price        BIGINT,
  product_url  TEXT UNIQUE,
  image_url    TEXT,           -- 대표 이미지 URL (저장 안 해도 되므로 NULL 허용)
  -- AI 생성 필드
  ai_info      TEXT,
  ai_review    TEXT,
  -- 타임스탬프
  created_at   TIMESTAMP DEFAULT NOW(),
  updated_at   TIMESTAMP DEFAULT NOW()
);

-- Images (이미지) : 상세/여러 장 이미지 관리
CREATE TABLE IF NOT EXISTS images (
  id          SERIAL PRIMARY KEY,
  product_id  INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url   TEXT UNIQUE NOT NULL,
  status      TEXT DEFAULT 'pending',
  sha256      CHAR(64),
  bytes       INTEGER,
  width       INTEGER,
  height      INTEGER,
  saved_path  TEXT,
  created_at  TIMESTAMP DEFAULT NOW(),
  updated_at  TIMESTAMP DEFAULT NOW()
);

-- Users (회원)
CREATE TABLE IF NOT EXISTS users (
  id         SERIAL PRIMARY KEY,
  email      TEXT UNIQUE NOT NULL,
  pw         TEXT NOT NULL,
  cell       TEXT,
  un         TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Sessions (세션)
CREATE TABLE IF NOT EXISTS sessions (
  session_id TEXT PRIMARY KEY,
  user_id    INTEGER REFERENCES users(id) ON DELETE SET NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP
);

-- Basket (장바구니: 세션 기반)
CREATE TABLE IF NOT EXISTS basket_items (
  id          SERIAL PRIMARY KEY,
  session_id  TEXT NOT NULL,
  product_id  INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  name        TEXT,
  img         TEXT,
  price       BIGINT,
  number      INTEGER DEFAULT 1,
  created_at  TIMESTAMP DEFAULT NOW(),
  updated_at  TIMESTAMP DEFAULT NOW(),
  UNIQUE (session_id, product_id)
);

-- Orders (주문 헤더)
CREATE TABLE IF NOT EXISTS orders (
  id          SERIAL PRIMARY KEY,
  user_id     INTEGER REFERENCES users(id) ON DELETE SET NULL,
  total_price BIGINT,
  address     TEXT,
  phone       TEXT,
  email       TEXT,
  created_at  TIMESTAMP DEFAULT NOW()
);

-- Order Items (주문 상세)
CREATE TABLE IF NOT EXISTS order_items (
  id         SERIAL PRIMARY KEY,
  order_id   INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,
  quantity   INTEGER NOT NULL,
  price      BIGINT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- =========================
-- Indexes
-- =========================
-- Images
CREATE INDEX IF NOT EXISTS idx_images_product_id  ON images(product_id);
CREATE INDEX IF NOT EXISTS idx_images_status      ON images(status);
CREATE UNIQUE INDEX IF NOT EXISTS ux_images_image_url ON images(image_url);

-- Products (추가 조회 최적화: 키워드/정렬/카테고리)
CREATE INDEX IF NOT EXISTS idx_products_keyword    ON products(keyword);
CREATE INDEX IF NOT EXISTS idx_products_sort       ON products(sort);
CREATE INDEX IF NOT EXISTS idx_products_category   ON products(category);
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_product_url ON products(product_url);

-- Sessions
CREATE INDEX IF NOT EXISTS idx_sessions_user_id    ON sessions(user_id);

-- Basket
CREATE INDEX IF NOT EXISTS idx_basket_session      ON basket_items(session_id);
CREATE INDEX IF NOT EXISTS idx_basket_product      ON basket_items(product_id);

-- Orders
CREATE INDEX IF NOT EXISTS idx_orders_user_id      ON orders(user_id);

-- Order Items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- =========================
-- Trigger (선택): updated_at 자동 갱신
-- =========================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'set_updated_at') THEN
    CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $tg$
    BEGIN
      NEW.updated_at := NOW();
      RETURN NEW;
    END;
    $tg$ LANGUAGE plpgsql;
  END IF;
END$$;

-- products, images, basket_items에 적용 (원하면 다른 테이블에도 추가)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname='trg_products_updated_at') THEN
    CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname='trg_images_updated_at') THEN
    CREATE TRIGGER trg_images_updated_at
    BEFORE UPDATE ON images
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname='trg_basket_items_updated_at') THEN
    CREATE TRIGGER trg_basket_items_updated_at
    BEFORE UPDATE ON basket_items
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
  END IF;
END$$;