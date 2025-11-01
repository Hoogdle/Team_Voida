# 네이버 쇼핑 파이프라인

네이버 쇼핑 Open API를 사용하여 상품 정보를 수집하고, 상세 페이지에서 이미지를 다운로드하는 파이프라인입니다.

## 주요 기능

1. **네이버 쇼핑 Open API 수집**: 키워드별로 상품 정보 수집
2. **데이터베이스 적재**: PostgreSQL에 상품 정보 저장
3. **상세 이미지 수집**: 상품 상세 페이지에서 이미지 URL 추출
   - HTTP 방식: 빠르지만 일부 페이지는 수집 불가
   - Selenium 방식: 느리지만 모든 페이지 수집 가능 (사람처럼 행동)
4. **이미지 다운로드**: 수집된 이미지 URL에서 이미지 다운로드

## 설치 및 설정

### 1. 패키지 설치

```bash
pip install -r requirements.txt
```

### 2. 환경 변수 설정

`.env.example` 파일을 `.env`로 복사하고 수정:

```bash
cp .env.example .env
```

`.env` 파일에서 다음 항목을 설정:
- `NAVER_CLIENT_ID`: 네이버 Open API Client ID
- `NAVER_CLIENT_SECRET`: 네이버 Open API Client Secret

### 3. PostgreSQL 실행

Docker Compose를 사용하여 PostgreSQL 실행:

```bash
docker-compose up -d
```

### 4. 데이터베이스 스키마 생성

```bash
psql -h localhost -U postgres -d naver_shop -f schema.sql
```

또는 직접 PostgreSQL에 접속하여 `schema.sql` 실행

## 사용 방법

### 전체 파이프라인 실행

```bash
python pipeline.py run --keywords 노트북 마우스 --pages 7 --max_images 10000 --details_mode selenium
```

### 개별 단계 실행

#### 1. 상품 정보 수집

```bash
python pipeline.py collect --keyword 노트북 --pages 7 --sort sim
```

#### 2. 데이터베이스 적재

```bash
python pipeline.py ingest
```

#### 3. 상세 이미지 URL 수집

HTTP 방식 (빠름):
```bash
python pipeline.py details --limit 300 --mode http
```

Selenium 방식 (느리지만 안정적):
```bash
python pipeline.py details --limit 150 --mode selenium --headless yes
```

#### 4. 이미지 다운로드

```bash
python pipeline.py download --limit 5000
```

## 명령어 옵션

### run (전체 파이프라인)

- `--keywords`: 수집할 키워드 리스트
- `--pages`: 키워드당 수집할 페이지 수 (기본값: 7)
- `--sort`: 정렬 방식 (sim, date, asc, dsc)
- `--max_images`: 최대 다운로드 이미지 수 (기본값: 10000)
- `--details_mode`: 상세 수집 모드 (http, selenium)
- `--headless`: 헤드리스 모드 (yes, no)
- `--proxy`: 프록시 서버 주소

### collect (상품 수집)

- `--keyword`: 수집할 키워드 (필수)
- `--pages`: 수집할 페이지 수 (기본값: 7)
- `--sort`: 정렬 방식 (기본값: sim)

### details (상세 이미지 URL 수집)

- `--limit`: 수집할 상품 수 (기본값: 300)
- `--mode`: 수집 모드 (http, selenium)
- `--headless`: 헤드리스 모드 (yes, no)
- `--proxy`: 프록시 서버 주소

### download (이미지 다운로드)

- `--limit`: 한 번에 다운로드할 이미지 수 (기본값: 5000)

## 디렉토리 구조

```
naver-shop-pipeline/
├── pipeline.py           # 메인 파이프라인 코드
├── requirements.txt      # Python 패키지 목록
├── schema.sql            # 데이터베이스 스키마
├── docker-compose.yml    # PostgreSQL 설정
├── .env                  # 환경 변수 (직접 생성)
├── .env.example          # 환경 변수 예시
├── README.md             # 이 파일
└── data/
    ├── raw/              # 수집된 JSON 데이터
    └── images/           # 다운로드된 이미지
```

## 주의사항

1. **네이버 API 제한**: Open API는 하루 25,000건의 호출 제한이 있습니다.
2. **Selenium 속도**: 사람처럼 행동하기 위해 각 상품당 60~120초 대기합니다.
3. **이미지 저장**: SHA256 해시 기반으로 중복 제거되어 저장됩니다.
4. **프록시 사용**: 대량 수집 시 프록시 사용을 권장합니다.

## 라이선스

MIT

