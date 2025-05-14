CREATE TABLE IF NOT EXISTS purchases (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    product_id INTEGER REFERENCES products(id),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
