CREATE TABLE IF NOT EXISTS addresses (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    address_line TEXT,
    city TEXT,
    postal_code TEXT,
    "default" BOOLEAN DEFAULT FALSE
);

