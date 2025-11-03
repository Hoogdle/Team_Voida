# pipeline.py
# 네이버 쇼핑(Open API) 수집 → PostgreSQL(products) 적재 → [상세페이지 이미지(shop-phinf)]만 다운로드
# 요구 패키지: requests, aiohttp, psycopg2-binary, python-dotenv, Pillow, selenium

import os, re, json, time, asyncio, hashlib, pathlib, mimetypes, io, random, sys
from html import unescape
from pathlib import Path

from bs4 import BeautifulSoup
import requests
import psycopg2, psycopg2.extras
from aiohttp import ClientTimeout
import aiohttp
from dotenv import load_dotenv
from PIL import Image

# server.py에서 리뷰 저장 함수 임포트 (같은 디렉토리)
try:
    from server import save_top3_reviews
except ImportError:
    # 임포트 실패 시 직접 구현 (fallback)
    def save_top3_reviews(product_id: int, reviews: list[str]) -> None:
        """리뷰 리스트(최대 3개)를 받아 DB에 업서트."""
        r1, r2, r3 = (reviews + [None, None, None])[:3]
        conn = get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute("""
                    INSERT INTO "review" ("product_id", "review1", "review2", "review3")
                    VALUES (%s, %s, %s, %s)
                    ON CONFLICT ("product_id") DO UPDATE
                    SET "review1" = EXCLUDED."review1",
                        "review2" = EXCLUDED."review2",
                        "review3" = EXCLUDED."review3";
                """, (product_id, r1, r2, r3))
            conn.commit()
        finally:
            conn.close()

# -------------------------------
# 환경설정
# -------------------------------
load_dotenv(dotenv_path=Path(__file__).with_name(".env"))

ROOT_DIR = Path(__file__).resolve().parent
RAW_DIR = ROOT_DIR / "data" / "raw"
IMAGES_DIR = Path(os.getenv("IMAGES_DIR", str(ROOT_DIR / "data" / "images")))
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:0000@localhost:5432/naver_shop")
CONCURRENCY = int(os.getenv("CONCURRENCY", "6"))
TIMEOUT = 20
RETRY = 3

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "*/*",
    "Referer": "https://shopping.naver.com/",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
}

# -------------------------------
# DB helpers
# -------------------------------
def get_conn():
    return psycopg2.connect(DATABASE_URL)

def ensure_schema_extensions():
    """상세 스캔 플래그와 인덱스가 없으면 생성"""
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute("""
        ALTER TABLE products ADD COLUMN IF NOT EXISTS detail_scanned BOOLEAN DEFAULT FALSE;
        CREATE INDEX IF NOT EXISTS idx_products_detail_scanned ON products(detail_scanned);
        """)
        conn.commit()

# -------------------------------
# 유틸
# -------------------------------
def ensure_dirs_for_sha(sha_hex: str, ext: str) -> pathlib.Path:
    d1, d2 = sha_hex[:2], sha_hex[2:4]
    outdir = IMAGES_DIR / d1 / d2
    outdir.mkdir(parents=True, exist_ok=True)
    return outdir / f"{sha_hex}{ext}"

def _clean_text(t: str | None) -> str | None:
    if not t:
        return t
    t = unescape(t)
    t = re.sub(r"<.*?>", "", t)
    return t.strip()

def guess_ext_from_ct(content_type: str | None) -> str:
    if not content_type:
        return ".bin"
    ct = content_type.split(";")[0].strip().lower()
    ext = mimetypes.guess_extension(ct) or ".bin"
    return ".jpg" if ext in [".jpe"] else ext

def get_img_size(content: bytes):
    try:
        with Image.open(io.BytesIO(content)) as im:
            return im.width, im.height
    except Exception:
        return None, None

# -------------------------------
# 1) 수집 (Open API)
# -------------------------------
def collect_openapi(keyword: str, sort: str = "sim", pages: int = 7, display: int = 80) -> int:
    """
    네이버 Open API → ./data/raw/<keyword>/<sort>_<start>.json 저장
    """
    client_id = (os.getenv("NAVER_CLIENT_ID") or "").strip()
    client_secret = (os.getenv("NAVER_CLIENT_SECRET") or "").strip()
    assert client_id and client_secret, "NAVER_CLIENT_ID / NAVER_CLIENT_SECRET 를 .env에 설정해주세요."

    raw_dir = RAW_DIR / keyword
    raw_dir.mkdir(parents=True, exist_ok=True)

    total = 0
    for p in range(pages):
        start = 1 + p * display
        url = (
            "https://openapi.naver.com/v1/search/shop.json"
            f"?query={requests.utils.quote(keyword)}&display={display}&start={start}&sort={sort}"
        )
        headers = {"X-Naver-Client-Id": client_id, "X-Naver-Client-Secret": client_secret}

        for attempt in range(3):
            try:
                r = requests.get(url, headers=headers, timeout=15)
                if r.status_code == 200:
                    data = r.json()
                    items = data.get("items", [])
                    with open(raw_dir / f"{sort}_{start:04d}.json", "w", encoding="utf-8") as f:
                        json.dump({"items": items}, f, ensure_ascii=False, indent=2)
                    total += len(items)
                    time.sleep(0.5)
                    break
                elif r.status_code == 429:
                    time.sleep(2 * (attempt + 1))
                else:
                    print(f"[WARN] {keyword} start={start} status={r.status_code} body={(r.text or '')[:180]}")
                    break
            except Exception as e:
                print(f"[WARN] {keyword} start={start} error: {e}")
                time.sleep(1 * (attempt + 1))

    print(f"[OPENAPI] {keyword} collected {total} items")
    return total

def collect_naver_shopping_data(keyword: str, sort: str = "sim", pages: int = 7, use_mcp: bool = False) -> int:
    # 키워드당 최대 15개만 수집
    return collect_openapi(keyword, sort=sort, pages=1, display=15)


# -------------------------------
# 2) DB 적재 (raw JSON → products)  [대표 이미지 사용 안 함]
# -------------------------------
def _clean_price(v):
    """₩, 쉼표, '원' 등 제거 후 int로 변환 (불가능 시 None)"""
    if v is None:
        return None
    s = re.sub(r'[^0-9]', '', str(v))
    try:
        return int(s) if s else None
    except Exception:
        return None

def upsert_product(cur, rec: dict) -> int:
    """상품을 업서트하고 product_id를 반환"""
    cur.execute("""
    INSERT INTO products (title, description, seller, category, price, product_url, img, keyword, sort)
    VALUES (%(title)s, %(description)s, %(seller)s, %(category)s, %(price)s, %(product_url)s, %(img)s, %(keyword)s, %(sort)s)
    ON CONFLICT (product_url) DO UPDATE SET
      title   = EXCLUDED.title,
      seller  = EXCLUDED.seller,
      category= EXCLUDED.category,
      price   = EXCLUDED.price,
      img     = COALESCE(EXCLUDED.img, products.img),
      keyword = EXCLUDED.keyword,
      sort    = EXCLUDED.sort
    RETURNING id;
    """, rec)
    result = cur.fetchone()
    return result[0] if result else None

def parse_and_ingest(raw_dir: str):
    """
    raw/<키워드>/<sort>_<start>.json → products upsert
    (대표 이미지 + keyword + sort 저장)
    """
    ensure_schema_extensions()
    total, inserted = 0, 0
    with get_conn() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
        for root, _, files in os.walk(raw_dir):
            for f in sorted(files):
                if not f.endswith(".json"):
                    continue
                fpath = os.path.join(root, f)

                # ex) sim_0001.json, date_0081.json ...
                m = re.match(r"([A-Za-z]+)_(\d+)\.json", f)
                file_sort_name = m.group(1) if m else None      # sim/date/asc/...
                page_start = int(m.group(2)) if m else 1        # 1, 81, 161 ...

                keyword = pathlib.Path(root).name               # 폴더명이 곧 키워드

                with open(fpath, "r", encoding="utf-8") as jf:
                    data = json.load(jf)

                items = data.get("items") if isinstance(data, dict) else data
                if not isinstance(items, list):
                    continue

                # 페이지 시작값 기준 전역 순위의 베이스 (0-based)
                base_rank = (page_start or 1) - 1

                for idx, it in enumerate(items):
                    total += 1
                    title = _clean_text(it.get("title"))
                    product_url = it.get("link") or it.get("product_url")
                    seller = it.get("mallName") or it.get("seller")
                    price = it.get("lprice") or it.get("price")

                    cats = [it.get(f"category{i}") for i in range(1, 5)]
                    cats = [c for c in cats if c]
                    category = "/".join(cats) if cats else None

                    image_url = it.get("image") or it.get("image_url")

                    # 1부터 시작하는 전역 순위
                    sort_rank = base_rank + idx + 1

                    rec = {
                        "title": title,
                        "description": None,
                        "seller": seller,
                        "category": category,
                        "price": _clean_price(price),
                        "product_url": product_url,
                        "img": image_url,
                        "keyword": keyword,        # ★ 추가
                        "sort": sort_rank,         # ★ 추가
                    }

                    if not product_url:
                        continue
                    try:
                        product_id = upsert_product(cur, rec)
                        inserted += 1
                        conn.commit()
                    except Exception as e:
                        conn.rollback()
                        print(f"[WARN] upsert fail ({keyword}): {e}")

        conn.commit()
    print(f"[INGEST] scanned={total}, upserted={inserted}")


# --- HTML fetch with fallback ---
from selenium.webdriver.chrome.options import Options as ChromeOptions

BROWSER_UA = HEADERS["User-Agent"]

REQ_HEADERS = {
    "User-Agent": BROWSER_UA,
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    "Connection": "keep-alive",
    "Upgrade-Insecure-Requests": "1",
    "Cache-Control": "no-cache",
    "Pragma": "no-cache",
    # 네이버 일부 페이지는 Referer 요구: 같은 도메인으로 맞춤
    "Referer": "https://search.shopping.naver.com/",
}

def fetch_html_requests(url: str, timeout: int = 20) -> str | None:
    try:
        with requests.Session() as s:
            s.headers.update(REQ_HEADERS)
            r = s.get(url, timeout=timeout, allow_redirects=True)
            if r.status_code == 200 and r.text:
                return r.text
            return None
    except Exception:
        return None

def fetch_html_selenium(url: str, wait_sec: float = 3.0, headless: bool = True) -> str | None:
    opts = ChromeOptions()
    if headless: opts.add_argument("--headless=new")
    opts.add_argument("--no-sandbox"); opts.add_argument("--disable-dev-shm-usage")
    opts.add_argument("--window-size=1280,2000")
    opts.add_argument(f"--user-agent={BROWSER_UA}")
    driver = webdriver.Chrome(options=opts)
    try:
        driver.get(url)
        time.sleep(wait_sec)
        return driver.page_source
    finally:
        try: driver.quit()
        except: pass

def fetch_html(url: str, prefer: str = "requests") -> str | None:
    """먼저 requests로 시도, 실패/차단/빈페이지면 selenium으로 폴백"""
    html = fetch_html_requests(url)
    if html and len(html) > 10000:  # 너무 짧은 스켈레톤은 무시
        return html
    # 418, 403, 동적로딩 등: 렌더링 폴백
    return fetch_html_selenium(url, wait_sec=3.0, headless=True)


# -------------------------------
# 홈 섹터(공식 허브 페이지) → products/home 적재
# -------------------------------

SECTOR_MAP = {
    1: "인기상품(베스트)",
    2: "오늘의 행사(기획전)",
    3: "특가(딜 모음)",
    4: "키워드/브랜드",
}

SECTOR_URLS = {
    1: "https://search.shopping.naver.com/best100",
    2: "https://search.shopping.naver.com/plan",
    3: "https://search.shopping.naver.com/deal/all",
    4: "https://search.shopping.naver.com/brand",
}

def _to_price_int(text: str | None):
    if not text: return None
    s = re.sub(r"[^\d]", "", text)
    return int(s) if s else None

def insert_home_if_absent(cur, product_id: int, sector: int):
    cur.execute('SELECT 1 FROM "home" WHERE product_id=%s AND sector=%s;', (product_id, sector))
    if not cur.fetchone():
        cur.execute('INSERT INTO "home" (product_id, sector) VALUES (%s,%s);', (product_id, sector))

# --- 섹터별 HTML 파서 ---
def parse_best100(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "lxml")
    items = []
    # 베스트 Top100 (페이지 구조가 바뀔 수 있음 → selector는 필요시 조정)
    # 안전하게 링크/이미지/제목/가격을 각각 유연하게 탐색
    for li in soup.select("li._itemSection, li.best_list_item, ul.best_list li"):
        a = li.select_one("a[href]")
        if not a: 
            continue
        img_tag = li.select_one("img")
        title_node = li.select_one(".txt, .title, .product_title, .name")
        price_node = li.select_one(".num, .price_num, .price, .sale_price, .product_price")
        items.append({
            "url": a.get("href"),
            "title": (title_node.get_text(strip=True) if title_node else None),
            "img": (img_tag.get("data-original") or img_tag.get("src")) if img_tag else None,
            "price": _to_price_int(price_node.get_text(strip=True) if price_node else None),
        })
    return items

def parse_plan(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "lxml")
    items = []
    for a in soup.select("a.thumbnail_link, a.plan_item, a[href*='smartstore.naver.com']"):
        title = a.select_one(".text, .title, .name")
        img = a.select_one("img")
        items.append({
            "url": a.get("href"),
            "title": title.get_text(strip=True) if title else None,
            "img": img.get("src") if img else None,
            "price": None,
        })
    return items

def parse_deal(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "lxml")
    items = []
    for a in soup.select("a.deal_item, a[href*='/deal/'], a[href*='smartstore.naver.com']"):
        title = a.select_one(".deal_tit, .title, .name")
        img = a.select_one("img")
        price_node = a.select_one(".price_num, .price, .sale_price")
        items.append({
            "url": a.get("href"),
            "title": title.get_text(strip=True) if title else None,
            "img": img.get("src") if img else None,
            "price": _to_price_int(price_node.get_text(strip=True) if price_node else None),
        })
    return items

def parse_brand(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "lxml")
    items = []
    for a in soup.select("a.brand_card_link, a[href*='brand'], a[href*='smartstore.naver.com']"):
        title = a.select_one(".brand_name, .title, .name")
        img = a.select_one("img")
        items.append({
            "url": a.get("href"),
            "title": title.get_text(strip=True) if title else None,
            "img": img.get("src") if img else None,
            "price": None,
        })
    return items

PARSERS = { 1: parse_best100, 2: parse_plan, 3: parse_deal, 4: parse_brand }

def crawl_home_sector(sector: int, limit: int = 20) -> int:
    """공식 허브 페이지에서 섹터별 상품을 수집하고 products/home에 반영"""
    assert sector in SECTOR_URLS, f"unknown sector: {sector}"
    url = SECTOR_URLS[sector]

    # ✅ 요청→Selenium 폴백 사용 (418/403/빈페이지/동적 DOM 대비)
    html = fetch_html(url)
    if not html:
        print(f"[HOME] sector={sector} ({SECTOR_MAP.get(sector)}) fetch failed")
        return 0

    parser = PARSERS[sector]
    items = parser(html)

    # 렌더링이 늦어 빈 리스트면 한 번 더 렌더링 폴백
    if not items:
        html2 = fetch_html_selenium(url, wait_sec=5.0, headless=True)
        if html2:
            items = parser(html2)

    if not items:
        print(f"[HOME] sector={sector} ({SECTOR_MAP.get(sector)}) no items parsed")
        return 0

    inserted = 0
    with get_conn() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
        for p in items[:limit]:
            if not p.get("url"): 
                continue
            rec = {
                "title": _clean_text(p.get("title")),
                "description": None,
                "seller": None,
                "category": None,
                "price": p.get("price"),
                "product_url": p.get("url"),
                "img": p.get("img"),
            }
            try:
                pid = upsert_product(cur, rec)  # 기존 upsert 재사용
                insert_home_if_absent(cur, pid, sector)
                inserted += 1
            except Exception as e:
                conn.rollback()
                print(f"[HOME] upsert/insert error: {e}")
        conn.commit()
    print(f"[HOME] sector={sector} ({SECTOR_MAP.get(sector)}) inserted={inserted}")
    return inserted

def crawl_home_all(limit_each: int = 20):
    total = 0
    for s in (1,2,3,4):
        total += crawl_home_sector(s, limit=limit_each)
        time.sleep(1.5)  # 부하/차단 방지
    print(f"[HOME] total inserted={total}")
    return total


# -------------------------------
# 리뷰 관련 함수
# -------------------------------
def ensure_product(product_url: str) -> int:
    """
    상품 URL로부터 product_id를 확보합니다.
    이미 존재하면 ID 반환, 없으면 생성 후 ID 반환.
    """
    with get_conn() as conn, conn.cursor() as cur:
        # 먼저 존재하는지 확인
        cur.execute("SELECT id FROM products WHERE product_url = %s;", (product_url,))
        row = cur.fetchone()
        if row:
            return row[0]
        
        # 없으면 기본 정보로 생성 (나중에 업데이트 가능)
        cur.execute("""
            INSERT INTO products (product_url, title)
            VALUES (%s, %s)
            RETURNING id;
        """, (product_url, f"Product {product_url}"))
        product_id = cur.fetchone()[0]
        conn.commit()
        return product_id

def ingest_one_product(product_url: str, reviews: list[str] | None = None):
    """
    하나의 상품을 처리하고 리뷰를 저장합니다.
    
    Args:
        product_url: 상품 URL
        reviews: 리뷰 리스트 (최대 3개), None이면 수집하지 않음
    """
    # 1) 상품 파싱 및 저장 → product_id 확보
    product_id = ensure_product(product_url)
    
    # 2) 리뷰 3개 수집 및 저장 (선택)
    if reviews:
        save_top3_reviews(product_id, reviews)
        print(f"[REVIEW] Saved {len(reviews)} reviews for product_id={product_id}")
    else:
        # 예시: 리뷰를 여기서 수집할 수도 있음
        # reviews = fetch_naver_top3_reviews(product_url)
        # if reviews:
        #     save_top3_reviews(product_id, reviews)
        pass

# -------------------------------
# 3-A) 상세페이지(HTTP)에서 설명 이미지 URL 수집 → images(pending)
# -------------------------------
IMG_URL_RE = re.compile(r'https?://[^\s"\'<>]+\.(?:jpg|jpeg|png|webp|gif)(?:\?[^\s"\'<>]*)?', re.IGNORECASE)

def normalize_img_url(u: str) -> str:
    try:
        from urllib.parse import urlsplit, urlunsplit, parse_qsl, urlencode
        sp = urlsplit(u)
        q = [(k, v) for (k, v) in parse_qsl(sp.query, keep_blank_values=True)
             if k.lower() not in {"type", "w", "h", "quality", "ttype", "udate", "src"}]
        return urlunsplit((sp.scheme, sp.netloc, sp.path, urlencode(q, doseq=True), sp.fragment))
    except Exception:
        return u

def _collect_urls_from_text(text: str) -> list[str]:
    urls = set()
    for m in IMG_URL_RE.finditer(text or ""):
        u = normalize_img_url(m.group(0))
        if not u.startswith("data:") and not u.lower().endswith(".svg"):
            urls.add(u)
    return list(urls)

def extract_gallery_urls(html: str) -> list[str]:
    urls = set()
    # Next.js
    m = re.search(r'<script[^>]+id="__NEXT_DATA__"[^>]*>([\s\S]*?)</script>', html, re.IGNORECASE)
    if m:
        urls.update(_collect_urls_from_text(m.group(1)))
    # SmartStore
    m = re.search(r'__PRELOADED_STATE__\s*=\s*(\{[\s\S]*?\});', html)
    if m:
        urls.update(_collect_urls_from_text(m.group(1)))
    # 일반 img/background
    urls.update(_collect_urls_from_text(html))
    out = [u for u in urls if "shop-phinf.pstatic.net" in u]  # 네이버 쇼핑 이미지 위주
    out = list(dict.fromkeys(out))[:30]  # 최대 30장
    return out

async def fetch_text(session: aiohttp.ClientSession, url: str) -> str | None:
    timeout = ClientTimeout(total=TIMEOUT)
    headers = HEADERS.copy()
    headers["Referer"] = url
    headers["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    try:
        async with session.get(url, headers=headers, allow_redirects=True, timeout=timeout) as r:
            if r.status == 200:
                return await r.text(errors="ignore")
    except Exception:
        return None
    return None

async def scan_product_detail(session, prod_id: int, product_url: str) -> int:
    html = await fetch_text(session, product_url)
    if not html:
        return 0
    urls = extract_gallery_urls(html)
    inserted = 0
    with get_conn() as conn, conn.cursor() as cur:
        for u in urls:
            try:
                cur.execute("""
                    INSERT INTO images (product_id, image_url, status)
                    VALUES (%s, %s, 'pending')
                    ON CONFLICT (image_url) DO NOTHING;
                """, (prod_id, u))
                inserted += cur.rowcount
            except Exception:
                conn.rollback()
        cur.execute("UPDATE products SET detail_scanned = TRUE WHERE id = %s;", (prod_id,))
        conn.commit()
    return inserted

def load_products_to_scan(limit=200):
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute("""
            SELECT id, product_url
              FROM products
             WHERE product_url IS NOT NULL
               AND (detail_scanned IS NOT TRUE)
             LIMIT %s;
        """, (limit,))
        return cur.fetchall()

async def collect_detail_images_async(limit=200):
    ensure_schema_extensions()
    rows = load_products_to_scan(limit=limit)
    if not rows:
        print("[DETAIL] nothing to scan")
        return

    sem = asyncio.Semaphore(max(2, min(6, CONCURRENCY // 2 or 2)))  # 상세는 보수적으로
    total_new = 0
    async with aiohttp.ClientSession() as session:
        async def one(r):
            nonlocal total_new
            pid, purl = r
            async with sem:
                try:
                    n = await scan_product_detail(session, pid, purl)
                    total_new += n
                except Exception as e:
                    print(f"[DETAIL] scan fail {pid}: {e}")

        await asyncio.gather(*(one(r) for r in rows))
    print(f"[DETAIL] scanned products={len(rows)}, new image URLs queued={total_new}")

def collect_detail_images(limit=200):
    asyncio.run(collect_detail_images_async(limit=limit))

# -------------------------------
# 3-B) Selenium(사람형) 상세 수집 → images(pending)
# -------------------------------
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support.ui import WebDriverWait

def _read_env_float(name: str, default: float) -> float:
    try: return float(os.getenv(name, str(default)))
    except Exception: return default

def _read_env_int(name: str, default: int) -> int:
    try: return int(os.getenv(name, str(default)))
    except Exception: return default

def build_chrome(headless: bool = True, proxy: str | None = None):
    opts = ChromeOptions()
    if headless:
        opts.add_argument("--headless=new")
    opts.add_argument("--lang=ko-KR")
    opts.add_argument("--window-size=1280,2000")
    ua = HEADERS["User-Agent"]
    opts.add_argument(f"--user-agent={ua}")
    opts.add_argument("--disable-gpu")
    opts.add_argument("--disable-infobars")
    opts.add_argument("--no-sandbox")
    opts.add_argument("--disable-dev-shm-usage")
    if proxy:
        opts.add_argument(f"--proxy-server={proxy}")
    driver = webdriver.Chrome(options=opts)
    driver.set_page_load_timeout(90)
    driver.set_script_timeout(90)
    return driver

def human_wait(min_s: float, max_s: float):
    time.sleep(random.uniform(min_s, max_s))

def human_scroll(driver, total_steps: int = 12):
    hmin = _read_env_float("SELENIUM_SCROLL_DELAY_MIN", 1.5)
    hmax = _read_env_float("SELENIUM_SCROLL_DELAY_MAX", 3.0)
    last_h = 0
    for _ in range(total_steps):
        driver.execute_script("window.scrollBy(0, Math.max(300, window.innerHeight*0.6));")
        human_wait(hmin, hmax)
        if random.random() < 0.2:
            driver.execute_script("window.scrollBy(0, -120);")
            human_wait(0.5, 1.2)
        new_h = driver.execute_script("return window.scrollY;")
        if new_h == last_h:
            break
        last_h = new_h

def _extract_from_img_tags(driver):
    urls = set()
    imgs = driver.find_elements(By.TAG_NAME, "img")
    for im in imgs:
        for attr in ("src", "data-src", "data-original", "data-lazy-src"):
            try:
                u = (im.get_attribute(attr) or "").strip()
                if u: urls.add(u)
            except Exception: pass
    return urls

def _extract_from_css_background(driver):
    urls = set()
    selectors = [
        "*[style*='background-image']",
        ".image, .thumb, .thumbnail, .gallery, .visual, .viewer, ._image",
    ]
    for sel in selectors:
        try:
            nodes = driver.find_elements(By.CSS_SELECTOR, sel)
            for n in nodes:
                style = (n.get_attribute("style") or "").lower()
                m = re.findall(r'url\((?:\"|\')?(.*?)(?:\"|\')?\)', style)
                for u in m:
                    if u: urls.add(u)
        except Exception: pass
    return urls

def _extract_from_page_source(driver):
    html = driver.page_source or ""
    return set(re.findall(r'https?://shop-phinf\.pstatic\.net/[^\s"\'<>]+\.(?:jpg|jpeg|png|gif)(?:\?[^\s"\'<>]*)?', html, flags=re.IGNORECASE))

def _normalize_img_url(u: str) -> str:
    try:
        from urllib.parse import urlsplit, urlunsplit, parse_qsl, urlencode
        sp = urlsplit(u)
        q = [(k,v) for (k,v) in parse_qsl(sp.query, keep_blank_values=True)
             if k.lower() not in {"type","w","h","quality","ttype","udate","src"}]
        return urlunsplit((sp.scheme, sp.netloc, sp.path, urlencode(q, doseq=True), sp.fragment))
    except Exception:
        return u

def selenium_collect_one_product(prod_id: int, product_url: str, headless: bool = True, proxy: str | None = None) -> int:
    driver = build_chrome(headless=headless, proxy=proxy)
    new_count = 0
    try:
        driver.get(product_url)

        init_min = _read_env_int("SELENIUM_INIT_WAIT_MIN", 20)
        init_max = _read_env_int("SELENIUM_INIT_WAIT_MAX", 40)
        human_wait(init_min, init_max)

        try:
            WebDriverWait(driver, 20).until(lambda d: d.execute_script("return document.readyState") == "complete")
        except Exception:
            pass

        try:
            ActionChains(driver).move_by_offset(random.randint(10,50), random.randint(10,30)).pause(0.5).perform()
        except Exception:
            pass

        human_scroll(driver, total_steps=12)

        urls = set()
        urls |= _extract_from_img_tags(driver)
        urls |= _extract_from_css_background(driver)
        urls |= _extract_from_page_source(driver)

        cleaned = []
        for u in urls:
            if not u or u.startswith("data:"):
                continue
            if "shop-phinf.pstatic.net" not in u:
                continue
            cleaned.append(_normalize_img_url(u))
        cleaned = list(dict.fromkeys(cleaned))[:40]

        with get_conn() as conn, conn.cursor() as cur:
            for u in cleaned:
                try:
                    cur.execute("""
                        INSERT INTO images (product_id, image_url, status)
                        VALUES (%s, %s, 'pending')
                        ON CONFLICT (image_url) DO NOTHING;
                    """, (prod_id, u))
                    new_count += cur.rowcount
                except Exception:
                    conn.rollback()
            cur.execute("UPDATE products SET detail_scanned = TRUE WHERE id = %s;", (prod_id,))
            conn.commit()
        return new_count
    finally:
        try: driver.quit()
        except Exception: pass

def collect_detail_images_selenium(limit=150, headless=True, proxy=None):
    ensure_schema_extensions()
    rows = load_products_to_scan(limit=limit)
    if not rows:
        print("[DETAIL/SELENIUM] nothing to scan")
        return
    total_new = 0
    for (pid, purl) in rows:
        try:
            n = selenium_collect_one_product(pid, purl, headless=headless, proxy=proxy)
            total_new += n
        except Exception as e:
            print(f"[DETAIL/SELENIUM] scan fail {pid}: {e}")
        # 사람처럼 천천히 접근: 60~120초 랜덤 대기(너무 느리면 .env로 조절)
        wait_lo = _read_env_int("SELENIUM_HUMAN_WAIT_MIN", 60)
        wait_hi = _read_env_int("SELENIUM_HUMAN_WAIT_MAX", 120)
        human_wait(wait_lo, wait_hi)
    print(f"[DETAIL/SELENIUM] scanned={len(rows)}, new image URLs queued={total_new}")

# -------------------------------
# 4) 이미지 다운로더 (images 큐만)
# -------------------------------
async def fetch(session: aiohttp.ClientSession, url: str):
    timeout = ClientTimeout(total=TIMEOUT)
    for attempt in range(1, RETRY + 1):
        try:
            async with session.get(url, headers={**HEADERS, "Referer": "https://smartstore.naver.com/"}, allow_redirects=True, timeout=timeout) as r:
                if r.status == 200:
                    ct = r.headers.get("Content-Type", "")
                    if not (ct and ct.lower().startswith("image")):
                        return None, None
                    content = await r.read()
                    # (옵션) 썸네일 거르기
                    # if len(content) < 20 * 1024:
                    #     return None, None
                    return content, ct
                elif r.status in (429, 403):
                    await asyncio.sleep(2 * attempt)
                else:
                    return None, None
        except Exception:
            await asyncio.sleep(1 * attempt)
    return None, None

async def worker(name: str, q: asyncio.Queue):
    async with aiohttp.ClientSession() as session:
        while True:
            item = await q.get()
            if item is None:
                q.task_done(); break

            prod_id, image_url = item
            content, ct = await fetch(session, image_url)

            if not content:
                with get_conn() as conn, conn.cursor() as cur:
                    try:
                        cur.execute("UPDATE images SET status='failed' WHERE product_id=%s AND image_url=%s;", (prod_id, image_url))
                        conn.commit()
                    except Exception:
                        pass
                q.task_done(); continue

            sha = hashlib.sha256(content).hexdigest()
            ext = guess_ext_from_ct(ct)
            path = ensure_dirs_for_sha(sha, ext)
            if not path.exists():
                path.parent.mkdir(parents=True, exist_ok=True)
                with open(path, "wb") as f:
                    f.write(content)

            width, height = get_img_size(content)
            with get_conn() as conn, conn.cursor() as cur:
                try:
                    cur.execute("""
                        UPDATE images
                           SET sha256=%s, bytes=%s, width=%s, height=%s, saved_path=%s, status='ok'
                         WHERE product_id=%s AND image_url=%s;
                    """, (sha, len(content), width, height, str(path), prod_id, image_url))
                    conn.commit()
                except Exception as e:
                    print(f"[WARN] image upsert fail: {e}")
            q.task_done()

def load_pending(limit: int = 5000):
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute("""
            SELECT i.product_id, i.image_url
              FROM images i
             WHERE COALESCE(i.status, 'pending') <> 'ok'
               AND i.image_url IS NOT NULL
             LIMIT %s;
        """, (limit,))
        return cur.fetchall()

async def download_all_async(batch_limit: int = 5000):
    rows = load_pending(limit=batch_limit)
    if not rows:
        print("[DL] nothing to download"); return

    q = asyncio.Queue()
    workers = [asyncio.create_task(worker(f"w{i}", q)) for i in range(CONCURRENCY)]
    for r in rows: await q.put(r)
    for _ in workers: await q.put(None)
    await q.join()
    for w in workers: await w
    print(f"[DL] processed={len(rows)}")

def download_all(batch_limit: int = 5000):
    asyncio.run(download_all_async(batch_limit=batch_limit))

# -------------------------------
# 5) 파이프라인
# -------------------------------
def run_full_pipeline(
    keywords: list[str],
    pages_per_keyword: int = 7,
    max_images: int = 10000,
    sort: str = "sim",
    details_mode: str = "selenium",   # "selenium" or "http"
    headless: bool = True,
    proxy: str | None = None,
    home_limit_each: int = 20,        
):

    """
    전체 파이프라인: 수집 → DB 적재 → 상세 이미지 URL 수집 → 다운로드
    * 대표 이미지는 저장하지 않음
    """
    print(f"[PIPELINE] keywords={len(keywords)}, pages/kw={pages_per_keyword}, target_images={max_images}, mode={details_mode}")

    total_collected = 0
    for kw in keywords:
        print(f"\n[COLLECT] {kw}")
        total_collected += collect_naver_shopping_data(kw, sort=sort, pages=pages_per_keyword)
    print(f"\n[PIPELINE] collected items: {total_collected}")

    print("[PIPELINE] ingest to DB ...")
    parse_and_ingest(str(RAW_DIR))

    print("[PIPELINE] update home sectors from official hubs ...")
    crawl_home_all(limit_each=home_limit_each)


    print("[PIPELINE] collect detail image URLs ...")
    if details_mode == "selenium":
        collect_detail_images_selenium(limit=150, headless=headless, proxy=proxy)
    else:
        collect_detail_images(limit=300)

    remain = max_images
    while remain > 0:
        batch = min(5000, remain)
        print(f"[PIPELINE] download batch={batch}")
        download_all(batch_limit=batch)
        remain -= batch

    print("[PIPELINE] completed")

# -------------------------------
# 6) CLI
# -------------------------------
if __name__ == "__main__":
    import argparse
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd")

    p_collect = sub.add_parser("collect", help="키워드 1개 수집(OpenAPI)")
    p_collect.add_argument("--keyword", required=True)
    p_collect.add_argument("--pages", type=int, default=7)
    p_collect.add_argument("--sort", default="sim")

    p_ingest = sub.add_parser("ingest", help="raw JSON → DB(products)")
    p_ingest.add_argument("--raw_dir", default=str(RAW_DIR))

    p_details = sub.add_parser("details", help="제품 상세페이지에서 설명/갤러리 이미지 URL 수집 → images(pending)")
    p_details.add_argument("--limit", type=int, default=300)
    p_details.add_argument("--mode", choices=["http","selenium"], default="selenium")
    p_details.add_argument("--headless", choices=["yes","no"], default="yes")
    p_details.add_argument("--proxy", default=os.getenv("SELENIUM_PROXY",""))

    p_download = sub.add_parser("download", help="images 큐 다운로드(상세 이미지 전용)")
    p_download.add_argument("--limit", type=int, default=5000)

    p_run = sub.add_parser("run", help="수집→적재→상세URL수집→다운로드")
    p_run.add_argument(
        "--keywords",
        nargs="+",
        default=[
            "노트북","물티슈","닭가슴살","애견간식","후드티","치약","햄","침대",
            "후라이팬","수영복","마우스","샴푸","백팩","필통","텀블러","보드게임",
            "포도","자전거","러그","시계"
        ],
    )
    p_run.add_argument("--pages", type=int, default=7)
    p_run.add_argument("--sort", default="sim")
    p_run.add_argument("--max_images", type=int, default=10000)
    p_run.add_argument("--details_mode", choices=["http","selenium"], default="selenium")
    p_run.add_argument("--headless", choices=["yes","no"], default="yes")
    p_run.add_argument("--proxy", default=os.getenv("SELENIUM_PROXY",""))

    # 👇 새로 추가
    p_home = sub.add_parser("home", help="공식 허브 페이지 기반 홈 섹터 채우기")
    p_home.add_argument("--sector", type=int, choices=[1,2,3,4], help="1:인기,2:행사,3:특가,4:브랜드. 생략시 전체")
    p_home.add_argument("--limit", type=int, default=20, help="섹터별 최대 삽입 개수")

    args = ap.parse_args()


    if args.cmd == "collect":
        collect_naver_shopping_data(args.keyword, sort=args.sort, pages=args.pages)
    elif args.cmd == "ingest":
        parse_and_ingest(args.raw_dir)
    elif args.cmd == "details":
        if args.mode == "selenium":
            collect_detail_images_selenium(
                limit=args.limit,
                headless=(args.headless.lower()=="yes"),
                proxy=(args.proxy or None)
            )
        else:
            collect_detail_images(limit=args.limit)
    elif args.cmd == "download":
        download_all(batch_limit=args.limit)
    elif args.cmd == "run":
        run_full_pipeline(
            args.keywords,
            pages_per_keyword=args.pages,
            max_images=args.max_images,
            sort=args.sort,
            details_mode=args.details_mode,
            headless=(args.headless.lower()=="yes"),
            proxy=(args.proxy or None),
        )

    elif args.cmd == "home":
        if args.sector:
            crawl_home_sector(args.sector, limit=args.limit)
        else:
            crawl_home_all(limit_each=args.limit)
    
    else:
        ap.print_help()

    
