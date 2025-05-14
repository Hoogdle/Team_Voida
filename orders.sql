CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    status TEXT DEFAULT 'pending',
    total_price NUMERIC,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
