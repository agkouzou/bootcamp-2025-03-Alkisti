TRUNCATE TABLE
    cart_items, orders, users, products,
    messages, threads
    RESTART IDENTITY CASCADE;

CREATE TABLE IF NOT EXISTS threads (
                                       id BIGSERIAL PRIMARY KEY,
                                       title VARCHAR(255),
                                       completion_model VARCHAR(255),
                                       has_unread_messages BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS messages (
                                        id BIGSERIAL PRIMARY KEY,
                                        content TEXT,
                                        thread_id BIGINT NOT NULL,
                                        is_completion BOOLEAN,
                                        completion_model VARCHAR(255),
                                        CONSTRAINT fk_thread FOREIGN KEY(thread_id) REFERENCES threads(id)
);

ALTER TABLE messages ALTER COLUMN content TYPE VARCHAR(2000);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    name TEXT,
    verified BOOLEAN DEFAULT FALSE,
    verification_token TEXT
);

DELETE FROM users
WHERE email = 'a.gkouzou@gmail.com';

CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        name TEXT NOT NULL,
                                        price NUMERIC(10, 2) NOT NULL,
                                        stock INT NOT NULL
);


-- Table for Orders and associated with user
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      status TEXT NOT NULL,
                                      created_at TIMESTAMP NOT NULL, -- DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table for cart items and associated with order
CREATE TABLE IF NOT EXISTS cart_items (
                                         id BIGSERIAL PRIMARY KEY,
                                         order_id BIGINT NOT NULL,
                                         product_id BIGINT NOT NULL,
                                         quantity INT NOT NULL,
                                         FOREIGN KEY (order_id) REFERENCES orders(id),
                                         FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create test users
INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('csekas@ctrlspace.dev', '123456', 'Chris Sekas', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('alkisti@ctrlspace.dev', '123456', 'Alkisti', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('nick@ctrlspace.dev', '123456789', 'Nick', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('george@ctrlspace.dev', '43f43gt45', 'George', true, null);

-- Create test products
INSERT INTO products (name, price, stock)
VALUES ('Macbook Pro', 2000.00, 5);

INSERT INTO products (name, price, stock)
VALUES ('Macbook Air', 1500.00, 10);

INSERT INTO products (name, price, stock)
VALUES ('Iphone 14', 600.00, 20);

INSERT INTO products (name, price, stock)
VALUES ('Iphone 14 Pro', 800.00, 15);

-- Insert Order 1 for Chris Sekas (using his email to look up the user id)
INSERT INTO orders (user_id, status, created_at)
SELECT id, 'pending', '2025-04-14 10:00:00'
FROM users
WHERE email = 'csekas@ctrlspace.dev';

-- Insert Cart Items for Order 1:

-- Cart item: 1 Macbook Pro (lookup product by name)
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'csekas@ctrlspace.dev')
              AND created_at = '2025-04-14 10:00:00'),
           (SELECT id FROM products WHERE name = 'Macbook Pro'),
           1
       );

-- Cart item: 2 iPhone 14s (lookup product by name)
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'csekas@ctrlspace.dev')
              AND created_at = '2025-04-14 10:00:00'),
           (SELECT id FROM products WHERE name = 'Iphone 14'),
           2
       );



-- Insert Order 2 for Alkisti
INSERT INTO orders (user_id, status, created_at)
SELECT id, 'completed', '2025-04-13 15:30:00'
FROM users
WHERE email = 'alkisti@ctrlspace.dev';

-- Insert Cart Item for Order 2:
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'alkisti@ctrlspace.dev')
              AND created_at = '2025-04-13 15:30:00'),
           (SELECT id FROM products WHERE name = 'Macbook Air'),
           1
       );



-- Insert Order 3 for Nick
INSERT INTO orders (user_id, status, created_at)
SELECT id, 'canceled', '2025-04-12 12:45:00'
FROM users
WHERE email = 'nick@ctrlspace.dev';

-- Insert Cart Item for Order 3:
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'nick@ctrlspace.dev')
              AND created_at = '2025-04-12 12:45:00'),
           (SELECT id FROM products WHERE name = 'Iphone 14 Pro'),
           1
       );


-- Insert Order 4 for Chris Sekas
INSERT INTO orders (user_id, status, created_at)
SELECT id, 'completed', '2025-04-11 08:20:00'
FROM users
WHERE email = 'csekas@ctrlspace.dev';

-- Insert Cart Items for Order 4:

-- Cart item: 2 Macbook Airs
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'csekas@ctrlspace.dev')
              AND created_at = '2025-04-11 08:20:00'),
           (SELECT id FROM products WHERE name = 'Macbook Air'),
           2
       );

-- Cart item: 1 iPhone 14
INSERT INTO cart_items (order_id, product_id, quantity)
VALUES (
           (SELECT id FROM orders
            WHERE user_id = (SELECT id FROM users WHERE email = 'csekas@ctrlspace.dev')
              AND created_at = '2025-04-11 08:20:00'),
           (SELECT id FROM products WHERE name = 'Iphone 14'),
           1
       );

