# pipeline.py
# 네이버 쇼핑(Open API) 수집 → PostgreSQL(products) 적재 → [상세페이지 이미지(shop-phinf)]만 다운로드
# 요구 패키지: requests, aiohttp, psycopg2-binary, python-dotenv, Pillow, selenium

import os, re, json, time, asyncio, hashlib, pathlib, mimetypes, io, random
from html import unescape
from pathlib import Path

import requests
import psycopg2, psycopg2.extras
from aiohttp import ClientTimeout
import aiohttp
from dotenv import load_dotenv
from PIL import Image

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

def upsert_product(cur, rec: dict):
    cur.execute("""
    INSERT INTO products (keyword, sort, page_start, title, description, seller, category, price, product_url, image_url)
    VALUES (%(keyword)s, %(sort)s, %(page_start)s, %(title)s, %(description)s, %(seller)s, %(category)s, %(price)s, %(product_url)s, %(image_url)s)
    ON CONFLICT (product_url) DO UPDATE SET
      title = EXCLUDED.title,
      seller = EXCLUDED.seller,
      category = EXCLUDED.category,
      price = EXCLUDED.price;
    """, rec)

def parse_and_ingest(raw_dir: str):
    """
    raw/<키워드>/<sort>_<start>.json → products upsert (대표 이미지 저장 안 함)
    """
    ensure_schema_extensions()
    total, inserted = 0, 0
    with get_conn() as conn, conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cur:
        for root, _, files in os.walk(raw_dir):
            for f in sorted(files):
                if not f.endswith(".json"):
                    continue
                fpath = os.path.join(root, f)
                m = re.match(r"([A-Za-z]+)_(\d+)\.json", f)
                sort = m.group(1) if m else None
                page_start = int(m.group(2)) if m else None
                keyword = pathlib.Path(root).name

                with open(fpath, "r", encoding="utf-8") as jf:
                    data = json.load(jf)

                items = data.get("items") if isinstance(data, dict) else data
                if not isinstance(items, list):
                    continue

                for it in items:
                    total += 1
                    title = _clean_text(it.get("title"))
                    product_url = it.get("link") or it.get("product_url")
                    seller = it.get("mallName") or it.get("seller")
                    price = it.get("lprice") or it.get("price")

                    cats = [it.get(f"category{i}") for i in range(1, 5)]
                    cats = [c for c in cats if c]
                    category = "/".join(cats) if cats else None

                    rec = {
                        "keyword": keyword,
                        "sort": sort,
                        "page_start": page_start,
                        "title": title,
                        "description": None,
                        "seller": seller,
                        "category": category,
                        "price": _clean_price(price),  # ← 수정된 부분
                        "product_url": product_url,
                        "image_url": None  # 대표 이미지는 저장하지 않음
                    }

                    if not product_url:
                        continue
                    try:
                        upsert_product(cur, rec)
                        inserted += 1
                        conn.commit()   # 트랜잭션 중지 방지..
                    except Exception as e:
                        conn.rollback()  # 트랜잭션 오류 발생 시 복구
                        print(f"[WARN] upsert fail ({keyword}): {e}")

        conn.commit()
    print(f"[INGEST] scanned={total}, upserted={inserted}")

# -------------------------------
# 3-A) 상세페이지(HTTP)에서 설명 이미지 URL 수집 → images(pending)
# -------------------------------
IMG_URL_RE = re.compile(r'https?://[^\s"\'<>]+\.(?:jpg|jpeg|png|gif)(?:\?[^\s"\'<>]*)?', re.IGNORECASE)

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
            async with session.get(url, headers=HEADERS, allow_redirects=True, timeout=timeout) as r:
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
    else:
        ap.print_help()
