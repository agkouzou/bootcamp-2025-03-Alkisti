CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    name TEXT
);

INSERT INTO users (email, password, name)
VALUES ('csekas@ctrlspace.dev', '123456', 'Chris Sekas');

INSERT INTO users (email, password, name)
VALUES ('alkisti@ctrlspace.dev', '123456', 'Alkisti');

INSERT INTO users (email, password, name)
VALUES ('nick@ctrlspace.dev', '123456', 'Nick');

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INT NOT NULL
);


INSERT INTO products (name, price, stock)
VALUES ('Macbook Pro', 2000.00, 5);

INSERT INTO products (name, price, stock)
VALUES ('Macbook Air', 1500.00, 10);

INSERT INTO products (name, price, stock)
VALUES ('Iphone 14', 600.00, 20);

INSERT INTO products (name, price, stock)
VALUES ('Iphone 14 Pro', 800.00, 15);

-- Table for Cart Items and associate with user


