# ✅ 1. Use official slim Python 3.10 base image
FROM python:3.10-slim

# ✅ 2. Set working directory inside the container
WORKDIR /app

# ✅ 3. Copy the requirements.txt file into the container
COPY requirements.txt .

# ✅ 4. Install all required Python packages
RUN pip install --no-cache-dir -r requirements.txt

# ✅ 5. Copy the entire project code into the container
COPY . .

# ✅ 6. Start the FastAPI app using uvicorn server
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
