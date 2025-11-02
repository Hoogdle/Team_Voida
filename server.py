mport os, re, io, json, hashlib, asyncio, mimetypes, pathlib, traceback, random, time
from pathlib import Path
from typing import Optional, Tuple, List
from datetime import datetime
from urllib.parse import urlsplit

import httpx
import psycopg2, psycopg2.extras
from fastapi import FastAPI, Body, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
from PIL import Image
from lxml import html as lxml_html

# =======================
# 환경설정
# =======================
load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:0000@localhost:5432/naver_shop")
IMAGES_DIR = Path(os.getenv("IMAGES_DIR", "./data/images")).resolve()
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

CONCURRENCY = int(os.getenv("CONCURRENCY", "2"))
TIMEOUT = int(os.getenv("TIMEOUT", "20"))

# VLM config (optional)
VLM_ENDPOINT = os.getenv("VLM_ENDPOINT", "").strip()
VLM_API_KEY = os.getenv("VLM_API_KEY", "").strip()
VLM_TIMEOUT = int(os.getenv("VLM_TIMEOUT", "30"))

# MCP blog search config (optional)
MCP_BLOG_ENDPOINT = os.getenv("MCP_BLOG_ENDPOINT", "").strip()
MCP_API_KEY = os.getenv("MCP_API_KEY", "").strip()
MCP_TIMEOUT = int(os.getenv("MCP_TIMEOUT", "15"))

HTTP2_ENABLED = os.getenv("HTTP2_ENABLED", "1") != "0"
REQ_MAX_RETRIES = int(os.getenv("REQUEST_MAX_RETRIES", "5"))
REQ_BACKOFF_BASE = float(os.getenv("REQUEST_BACKOFF_BASE", "0.8"))
REQ_BACKOFF_FACTOR = float(os.getenv("REQUEST_BACKOFF_FACTOR", "2"))
REQ_BACKOFF_JITTER = float(os.getenv("REQUEST_BACKOFF_JITTER", "0.4"))
PER_HOST_MIN_INTERVAL = float(os.getenv("PER_HOST_MIN_INTERVAL", "2.0"))  # 초

# 기본 UA + 간단 로테이션
UA_DEFAULT = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
              "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
UA_POOL = [
    UA_DEFAULT,
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36",
]
def pick_ua() -> str:
    return random.choice(UA_POOL)

HEADERS = {
    "User-Agent": UA_DEFAULT,
    "Accept": "*/*",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    "Referer": "https://shopping.naver.com/"
}

app = FastAPI(title="Shopping MCP (per-host pacing)", version="1.2.0")

def db():
    return psycopg2.connect(DATABASE_URL)

# =======================
# 스키마 자동 보강
# =======================
SCHEMA_SQL = """
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

CREATE TABLE IF NOT EXISTS images (
  id SERIAL PRIMARY KEY,
  product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url TEXT UNIQUE,
  status TEXT DEFAULT 'pending',
  sha256 CHAR(64),
  bytes INTEGER,
  width INTEGER,
  height INTEGER,
  saved_path TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

ALTER TABLE products ADD COLUMN IF NOT EXISTS product_url TEXT;
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_product_url ON products(product_url);
ALTER TABLE products ADD COLUMN IF NOT EXISTS img TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS ai_info TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS ai_review TEXT;

ALTER TABLE images ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'pending';
ALTER TABLE images ADD COLUMN IF NOT EXISTS sha256 CHAR(64);
ALTER TABLE images ADD COLUMN IF NOT EXISTS bytes INTEGER;
ALTER TABLE images ADD COLUMN IF NOT EXISTS width INTEGER;
ALTER TABLE images ADD COLUMN IF NOT EXISTS height INTEGER;
ALTER TABLE images ADD COLUMN IF NOT EXISTS saved_path TEXT;
CREATE UNIQUE INDEX IF NOT EXISTS ux_images_image_url ON images(image_url);
"""

@app.on_event("startup")
def _ensure_schema():
    with db() as conn, conn.cursor() as cur:
        cur.execute(SCHEMA_SQL)
        conn.commit()

# =======================
# 파일/이미지 유틸
# =======================
def ensure_dirs_for_sha(sha_hex: str, ext: str) -> pathlib.Path:
    d1, d2 = sha_hex[:2], sha_hex[2:4]
    outdir = IMAGES_DIR / d1 / d2
    outdir.mkdir(parents=True, exist_ok=True)
    return outdir / f"{sha_hex}{ext}"

def guess_ext_from_ct(ct: Optional[str]) -> str:
    if not ct: return ".bin"
    base = ct.split(";")[0].strip().lower()
    ext = mimetypes.guess_extension(base) or ".bin"
    if ext == ".jpe": ext = ".jpg"
    return ext

def get_img_size(content: bytes) -> Tuple[Optional[int], Optional[int]]:
    try:
        with Image.open(io.BytesIO(content)) as im:
            return im.width, im.height
    except Exception:
        return None, None

def _to_int_safe(x) -> Optional[int]:
    try:
        if x is None: return None
        s = str(x)
        digits = re.sub(r"[^\d]", "", s)
        return int(digits) if digits else None
    except Exception:
        return None

# =======================
# 파싱 유틸 (jpg/png 기반에서 webp, gif 추가)
# =======================
# 이미지 확장자 추가
IMG_URL_RE = re.compile(
    r'https?://[^\s"\'<>]+\.(?:jpg|jpeg|png|webp|gif)(?:\?[^\s"\'<>]*)?',
    re.IGNORECASE
)

def normalize_img_url(u: str) -> str:
    try:
        from urllib.parse import urlsplit, urlunsplit, parse_qsl, urlencode
        sp = urlsplit(u)
        q = [(k, v) for (k, v) in parse_qsl(sp.query, keep_blank_values=True)
             if k.lower() not in {"type","w","h","quality","ttype","udate","src"}]
        return urlunsplit((sp.scheme, sp.netloc, sp.path, urlencode(q, doseq=True), sp.fragment))
    except Exception:
        return u

def _collect_urls(text: str) -> List[str]:
    seen, out = set(), []
    for m in IMG_URL_RE.finditer(text or ""):
        u = normalize_img_url(m.group(0))
        if u not in seen:
            seen.add(u)
            out.append(u)
    return out

def extract_json_blob(html: str) -> dict:
    m = re.search(r'__PRELOADED_STATE__\s*=\s*(\{[\s\S]*?\});', html)
    if m:
        try: return json.loads(m.group(1))
        except Exception: pass
    m = re.search(r'<script[^>]+id="__NEXT_DATA__"[^>]*>([\s\S]*?)</script>', html, re.IGNORECASE)
    if m:
        try: return json.loads(m.group(1))
        except Exception: pass
    return {}

def extract_core_info(url: str, html: str) -> dict:
    tree = lxml_html.fromstring(html)

    # title
    title = None
    for xp in [
        '//meta[@property="og:title"]/@content',
        '//h3/text()',
        '//title/text()'
    ]:
        v = tree.xpath(xp)
        if v:
            title = (v[0] or "").strip()
            if title: break

    blob = extract_json_blob(html)

    # price
    price = None
    try:
        for path in [
            ("smartStoreV2","product","price","sellPrice"),
            ("product","A","price","sellPrice"),
            ("goods","purchasePrice"),
            ("products","A","price","sellPrice"),
        ]:
            cur = blob
            for k in path: cur = cur[k]
            price = _to_int_safe(cur)
            if price is not None: break
    except Exception:
        pass

    # seller
    seller = None
    try:
        for path in [
            ("smartStoreV2","product","store","shopName"),
            ("channel","shop","name"),
            ("store","shopName"),
        ]:
            cur = blob
            for k in path: cur = cur[k]
            seller = (str(cur) if cur is not None else "").strip()
            if seller: break
    except Exception:
        pass

    # category
    category = None
    try:
        for path in [
            ("smartStoreV2","product","category","fullCategoryNamePath"),
            ("product","A","category","fullCategoryNamePath"),
        ]:
            cur = blob
            for k in path: cur = cur[k]
            if isinstance(cur, list):
                category = "/".join([str(x) for x in cur if x])
                break
    except Exception:
        pass

    return {"url": url, "title": title, "price": price, "seller": seller, "category": category}

def extract_detail_image_urls(html: str) -> List[str]:
    urls = set()
    blob = extract_json_blob(html)
    if blob:
        urls.update(_collect_urls(json.dumps(blob, ensure_ascii=False)))
    urls.update(_collect_urls(html))
    out = [u for u in urls if "shop-phinf.pstatic.net" in u]
    out = list(dict.fromkeys(out))[:40]
    return out

# =======================
# per-host pacing + 재시도
# =======================
_last_req_at = {}           # host -> timestamp
_host_lock = asyncio.Lock() # serialize updates

async def pace_by_host(url: str):
    """같은 호스트로 연속 호출 시 최소 간격 보장"""
    host = urlsplit(url).netloc
    async with _host_lock:
        now = time.monotonic()
        last = _last_req_at.get(host, 0.0)
        wait = (last + PER_HOST_MIN_INTERVAL) - now
        if wait > 0:
            await asyncio.sleep(wait)
        _last_req_at[host] = time.monotonic()

async def get_with_retry(url: str, *, client: httpx.AsyncClient) -> str:
    last_exc = None
    for attempt in range(REQ_MAX_RETRIES):
        # 호스트별 최소 간격 + UA 회전 + 지터
        await pace_by_host(url)
        client.headers["User-Agent"] = pick_ua()
        jitter = random.uniform(0, REQ_BACKOFF_JITTER)
        try:
            r = await client.get(url, follow_redirects=True, timeout=TIMEOUT)
            r.raise_for_status()   # 2xx 아니면 예외
            return r.text
        except httpx.HTTPStatusError as e:
            status = e.response.status_code
            if status in (429, 403) or 500 <= status < 600:
                ra = e.response.headers.get("Retry-After")
                if ra:
                    try:
                        sleep_s = max(float(ra), REQ_BACKOFF_BASE) + jitter
                    except Exception:
                        sleep_s = (REQ_BACKOFF_BASE * (REQ_BACKOFF_FACTOR ** attempt)) + jitter
                else:
                    sleep_s = (REQ_BACKOFF_BASE * (REQ_BACKOFF_FACTOR ** attempt)) + jitter
                await asyncio.sleep(sleep_s)
                last_exc = e
                continue
            raise
        except (httpx.ReadTimeout, httpx.ConnectTimeout, httpx.RemoteProtocolError) as e:
            sleep_s = (REQ_BACKOFF_BASE * (REQ_BACKOFF_FACTOR ** attempt)) + jitter
            await asyncio.sleep(sleep_s)
            last_exc = e
            continue
        except Exception as e:
            last_exc = e
            break
    if last_exc:
        raise last_exc
    raise HTTPException(status_code=500, detail="unexpected retry failure")

# =======================
# DB SQL
# =======================
UPSERT_PRODUCT_SQL = """
INSERT INTO products (product_url, title, price, seller, category, created_at, updated_at)
VALUES (%(url)s, %(title)s, %(price)s, %(seller)s, %(category)s, NOW(), NOW())
ON CONFLICT (product_url) DO UPDATE SET
  title = COALESCE(EXCLUDED.title, products.title),
  price = COALESCE(EXCLUDED.price, products.price),
  seller = COALESCE(EXCLUDED.seller, products.seller),
  category = COALESCE(EXCLUDED.category, products.category),
  updated_at = NOW()
RETURNING id;
"""

INSERT_IMAGE_SAFE_SQL = """
INSERT INTO images (product_id, image_url, status, created_at, updated_at)
SELECT %s, %s, 'pending', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM images WHERE image_url = %s);
"""

UPDATE_IMAGE_OK_SQL = """
UPDATE images SET status='ok', sha256=%s, bytes=%s, width=%s, height=%s, saved_path=%s, updated_at=NOW()
WHERE product_id=%s AND image_url=%s;
"""

UPDATE_IMAGE_FAIL_SQL = """
UPDATE images SET status='failed', updated_at=NOW()
WHERE product_id=%s AND image_url=%s;
"""

# =======================
# Review 업서트 함수
# =======================
UPSERT_REVIEW_SQL = """
INSERT INTO "review" ("product_id", "review1", "review2", "review3")
VALUES (%s, %s, %s, %s)
ON CONFLICT ("product_id") DO UPDATE
SET "review1" = EXCLUDED."review1",
    "review2" = EXCLUDED."review2",
    "review3" = EXCLUDED."review3";
"""

def save_top3_reviews(product_id: int, reviews: List[str]) -> None:
    """리뷰 리스트(최대 3개)를 받아 DB에 업서트."""
    r1, r2, r3 = (reviews + [None, None, None])[:3]
    conn = db()
    try:
        with conn.cursor() as cur:
            cur.execute(UPSERT_REVIEW_SQL, (product_id, r1, r2, r3))
        conn.commit()
    finally:
        conn.close()

# =======================
# 이미지 다운로드
# =======================
async def fetch_image(client: httpx.AsyncClient, url: str):
    try:
        r = await client.get(url, headers=client.headers, follow_redirects=True, timeout=TIMEOUT)
        if r.status_code == 200:
            ct = (r.headers.get("Content-Type") or "").lower()
            if ct.startswith("image/jpeg") or ct.startswith("image/png") \
               or ct.startswith("image/webp") or ct.startswith("image/gif"):
                return r.content, ct
    except Exception:
        return None, None
    return None, None

async def download_images(product_id: int, urls: List[str]):
    limits = httpx.Limits(max_connections=max(1, CONCURRENCY))
    async with httpx.AsyncClient(headers=HEADERS.copy(), limits=limits, http2=HTTP2_ENABLED) as client:
        sem = asyncio.Semaphore(CONCURRENCY)

        async def one(u: str):
            async with sem:
                # 호스트별 페이싱 + 소량 지연
                await pace_by_host(u)
                await asyncio.sleep(random.uniform(0, 0.2))
                client.headers["User-Agent"] = pick_ua()

                client.headers["Referer"] = "https://smartstore.naver.com/"

                content, ct = await fetch_image(client, u)
                if not content:
                    with db() as conn, conn.cursor() as cur:
                        cur.execute(UPDATE_IMAGE_FAIL_SQL, (product_id, u))
                        conn.commit()
                    return

                sha = hashlib.sha256(content).hexdigest()
                ext = guess_ext_from_ct(ct)
                if ext not in (".jpg", ".jpeg", ".png"):
                    with db() as conn, conn.cursor() as cur:
                        cur.execute(UPDATE_IMAGE_FAIL_SQL, (product_id, u))
                        conn.commit()
                    return
                if ext == ".jpeg": ext = ".jpg"

                path = ensure_dirs_for_sha(sha, ext)
                if not path.exists():
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_bytes(content)
                w, h = get_img_size(content)

                with db() as conn, conn.cursor() as cur:
                    cur.execute(UPDATE_IMAGE_OK_SQL, (sha, len(content), w, h, str(path), product_id, u))
                    conn.commit()

        await asyncio.gather(*(one(u) for u in urls))

# =======================
# API 모델/엔드포인트
# =======================
class IngestRequest(BaseModel):
    url: str  # 문자열로 받고 내부에서 http(s)://만 체크 → 422 완화

class IngestResponse(BaseModel):
    product_id: int
    title: Optional[str]
    price: Optional[int]
    seller: Optional[str]
    category: Optional[str]
    images_found: int
    message: str = "ok"

# =======================
# Compatibility models (맞춤 스키마)
# =======================
class ProductSummary(BaseModel):
    id: int
    name: Optional[str]
    description: Optional[str]
    price: Optional[float]
    image_url: Optional[str] = None
    category: Optional[str] = None

    class Config:
        from_attributes = True
        orm_mode = True

class ProductDetail(BaseModel):
    product_id: int
    name: str
    image_url: Optional[str]
    price: float
    ai_info: str
    ai_review: str

    class Config:
        from_attributes = True

class SearchRequest(BaseModel):
    search: str

# -------------------- Basket --------------------
class EmailRequest(BaseModel):
    email: Optional[str]

class ProductIDRequest(BaseModel):
    product_id: int
    session_id: str

class BasketItem(BaseModel):
    product_id: int
    img: str
    name: str
    price: float
    number: int

    class Config:
        from_attributes = True

class BasketRequest(BaseModel):
    session_id: str

class BasketModifyRequest(BaseModel):
    session_id: str
    product_id: int

class BasketInsertRequest(BaseModel):
    session_id: str
    product_id: int

class OneItemRequest(BaseModel):
    session_id: str
    product_id: int


# -------------------- Payment --------------------
class PaymentResponse(BaseModel):
    address: Optional[str] = None
    phone: Optional[str] = None
    email: str
    item: List[BasketItem]


class OneItemPaymentResponse(BaseModel):
    address: str
    phone: str
    email: str
    item: List[BasketItem]

class BasketPayment(BaseModel):
    session_id: str

# -------------------- Order --------------------
class OrderItemCreate(BaseModel):
    product_id: int
    quantity: int
    price: float


class OrderCreate(BaseModel):
    user_id: int
    total_price: float
    created_at: datetime
    items: List[OrderItemCreate]


class OrderResponse(BaseModel):
    id: int
    user_id: int
    total_price: float
    created_at: datetime

    model_config = {
        "from_attributes": True
    }


class TodaySaleItemResponse(BaseModel):
    id: int
    name: str
    description: str
    price: float
    image_url: Optional[str] = None
    category: Optional[str] = None

    class Config:
        from_attributes = True


# -------------------- User --------------------
class SignUpRequest(BaseModel):
    email: str
    pw: str
    cell: str
    un: str


class LoginRequest(BaseModel):
    email: str
    pw: str


class LoginResponse(BaseModel):
    session_id: str


class UserNameRequest(BaseModel):
    un: str

class ReviewRequest(BaseModel):
    product_id: int

class ReviewProvide(BaseModel):
    ai_review: str

class AssistantCategory(BaseModel):
    voiceInput: str

class AssistantSearch(BaseModel):
    voiceInput: str

class CancelAIRequest(BaseModel):
    session_id: str

# -------------------- Review --------------------
class ReviewIn(BaseModel):
    product_id: int
    reviews: list[str]  # 최대 3개 넣어주세요.

# =======================
# VLM summary generator (optional)
# =======================
def _get_primary_image_url(product_id: int) -> Optional[str]:
    with db() as conn, conn.cursor() as cur:
        cur.execute(
            """
            SELECT i.image_url
              FROM images i
             WHERE i.product_id=%s AND COALESCE(i.status,'pending')='ok'
             ORDER BY i.id ASC LIMIT 1
            """,
            (product_id,)
        )
        r = cur.fetchone()
        return r[0] if r else None

def _generate_vlm_summary(name: str, category: Optional[str], price: Optional[float], image_url: Optional[str]) -> tuple[Optional[str], Optional[str]]:
    if not VLM_ENDPOINT or not VLM_API_KEY:
        return None, None
    payload = {
        "name": name,
        "category": category,
        "price": price,
        "image_url": image_url,
        "purpose": "naver_shop_product_summary"
    }
    headers = {
        "Authorization": f"Bearer {VLM_API_KEY}",
        "Content-Type": "application/json"
    }
    try:
        with httpx.Client(timeout=VLM_TIMEOUT) as client:
            resp = client.post(VLM_ENDPOINT, json=payload, headers=headers)
            resp.raise_for_status()
            data = resp.json() if resp.headers.get("content-type", "").lower().startswith("application/json") else {}
            ai_info = (data.get("ai_info") or data.get("summary") or "")[:4000]
            ai_review = (data.get("ai_review") or data.get("review") or "")[:4000]
            ai_info = ai_info or None
            ai_review = ai_review or None
            return ai_info, ai_review
    except Exception:
        return None, None

def _ensure_ai_cached(product_id: int, name: str, category: Optional[str], price: Optional[float], ai_info: Optional[str], ai_review: Optional[str]) -> tuple[Optional[str], Optional[str]]:
    if ai_info and ai_review:
        return ai_info, ai_review
    image_url = _get_primary_image_url(product_id)
    gen_info, gen_review = _generate_vlm_summary(name=name or "", category=category, price=price, image_url=image_url)
    if not gen_info and not gen_review:
        return ai_info, ai_review
    with db() as conn, conn.cursor() as cur:
        cur.execute(
            "UPDATE products SET ai_info=COALESCE(ai_info,%s), ai_review=COALESCE(ai_review,%s), updated_at=NOW() WHERE id=%s",
            (gen_info, gen_review, product_id)
        )
        conn.commit()
    return ai_info or gen_info, ai_review or gen_review

# =======================
# MCP blog search (optional)
# =======================
class ReviewSnippet(BaseModel):
    title: str
    url: str
    snippet: str

class ProductSummaryWithReviews(ProductSummary):
    reviews: List[ReviewSnippet] = []

async def _search_blog_reviews_async(topic: str, limit: int = 3) -> List[ReviewSnippet]:
    if not MCP_BLOG_ENDPOINT or not MCP_API_KEY:
        return []
    q = f"{topic} 리뷰"
    params = {"q": q, "limit": limit}
    headers = {"Authorization": f"Bearer {MCP_API_KEY}"}
    try:
        async with httpx.AsyncClient(timeout=MCP_TIMEOUT) as client:
            r = await client.get(MCP_BLOG_ENDPOINT, params=params, headers=headers)
            r.raise_for_status()
            data = r.json()
            items = data.get("items") if isinstance(data, dict) else data
            out = []
            if isinstance(items, list):
                for it in items[:limit]:
                    title = (it.get("title") or it.get("name") or "").strip()
                    url = (it.get("url") or it.get("link") or "").strip()
                    snippet = (it.get("snippet") or it.get("summary") or it.get("description") or "").strip()
                    if title and url:
                        out.append(ReviewSnippet(title=title, url=url, snippet=snippet))
            return out
    except Exception:
        return []

@app.get("/health")
def health():
    return {"ok": True}

@app.post("/ingest", response_model=IngestResponse)
async def ingest(req: IngestRequest = Body(...)) -> IngestResponse:
    try:
        u = (req.url or "").strip()
        if not (u.startswith("http://") or u.startswith("https://")):
            raise HTTPException(status_code=422, detail="url must start with http:// or https://")

        # 1) 페이지 가져오기 (재시도 + per-host pacing)
        limits = httpx.Limits(max_connections=max(1, CONCURRENCY))
        async with httpx.AsyncClient(headers=HEADERS.copy(), limits=limits, http2=HTTP2_ENABLED) as client:
            html = await get_with_retry(u, client=client)

        # 2) 핵심 정보 추출
        core = extract_core_info(u, html)

        # 3) products upsert
        with db() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
            cur.execute(UPSERT_PRODUCT_SQL, core)
            pid = cur.fetchone()[0]
            conn.commit()

        # 4) 상세 이미지 URL 수집 (shop-phinf, jpg/png, 최대 40)
        urls = extract_detail_image_urls(html)
        with db() as conn, conn.cursor() as cur:
            for img in urls:
                cur.execute(INSERT_IMAGE_SAFE_SQL, (pid, img, img))
            conn.commit()

        # 5) 이미지 즉시 저장 (per-host pacing 적용)
        await download_images(pid, urls)

        return IngestResponse(
            product_id=pid,
            title=core.get("title"),
            price=core.get("price"),
            seller=core.get("seller"),
            category=core.get("category"),
            images_found=len(urls),
            message="ok"
        )

    except HTTPException:
        raise
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"ingest failed: {type(e).__name__}: {e}")

# =======================
# Product listing/search/detail endpoints
# =======================
@app.get("/products", response_model=List[ProductSummary])
def list_products(search: Optional[str] = None) -> List[ProductSummary]:
    q = []
    args = []
    if search:
        q.append("(p.title ILIKE %s OR p.category ILIKE %s OR p.seller ILIKE %s)")
        v = f"%{search}%"
        args += [v, v, v]
    where = ("WHERE " + " AND ".join(q)) if q else ""
    sql = f"""
        SELECT p.id,
               p.title AS name,
               p.description,
               p.price::float AS price,
               COALESCE( 
                (SELECT i.image_url FROM images i WHERE i.product_id = p.id AND COALESCE(i.status,'pending') = 'ok' ORDER BY i.id ASC LIMIT 1),
                p.img
               ) AS image_url,
               p.category
          FROM products p
         {where}
         ORDER BY p.updated_at DESC, p.id DESC
         LIMIT 100
    """
    with db() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
        cur.execute(sql, args)
        rows = cur.fetchall()
        return [ProductSummary(
            id=row["id"],
            name=row["name"],
            description=row["description"],
            price=row["price"],
            image_url=row["image_url"],
            category=row["category"],
        ) for row in rows]

@app.post("/search", response_model=List[ProductSummary])
def search_products(req: SearchRequest = Body(...)) -> List[ProductSummary]:
    return list_products(search=(req.search or "").strip() or None)

@app.post("/search_reviews")
async def search_with_reviews(req: SearchRequest = Body(...)) -> List[ProductSummaryWithReviews]:
    # 기본 검색 결과 가져오기 (최대 10개)
    items = list_products(search=(req.search or "").strip() or None)[:10]
    # 병렬로 각 상품 제목 기준 블로그 리뷰 3개씩 수집
    async def enrich(item: ProductSummary) -> ProductSummaryWithReviews:
        reviews = await _search_blog_reviews_async(item.name or (req.search or ""), limit=3)
        return ProductSummaryWithReviews(**item.dict(), reviews=reviews)

    enriched: List[ProductSummaryWithReviews] = []
    for it in items:
        enriched.append(await enrich(it))
    return enriched

@app.get("/products/{product_id}", response_model=ProductDetail)
def get_product_detail(product_id: int) -> ProductDetail:
    with db() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
        cur.execute(
            """
            SELECT p.id,
                   p.title AS name,
                   p.price::float AS price,
                   p.ai_info,
                   p.ai_review,
                   COALESCE(
                   (
                     SELECT i.image_url FROM images i
                      WHERE i.product_id = p.id AND COALESCE(i.status,'pending') = 'ok'
                      ORDER BY i.id ASC LIMIT 1
                   ),
                   p.img
                ) AS image_url,
                   p.category
              FROM products p
             WHERE p.id = %s
            """,
            (product_id,)
        )
        row = cur.fetchone()
        if not row:
            raise HTTPException(status_code=404, detail="product not found")
        ai_info, ai_review = _ensure_ai_cached(
            product_id=row["id"],
            name=row["name"] or "",
            category=row["category"],
            price=row["price"] or 0.0,
            ai_info=row["ai_info"],
            ai_review=row["ai_review"],
        )
        return ProductDetail(
            product_id=row["id"],
            name=row["name"] or "",
            image_url=row["image_url"],
            price=row["price"] or 0.0,
            ai_info=ai_info or "",
            ai_review=ai_review or "",
        )

@app.post("/reviews/upsert")
def upsert_reviews(payload: ReviewIn = Body(...)):
    """리뷰를 업서트하는 엔드포인트. 최대 3개의 리뷰를 받아서 저장합니다."""
    save_top3_reviews(payload.product_id, payload.reviews)
    return {"ok": True, "product_id": payload.product_id, "count": len(payload.reviews)}

