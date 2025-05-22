CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    image_url TEXT,
    price VARCHAR,
    description TEXT,
    category VARCHAR
);
