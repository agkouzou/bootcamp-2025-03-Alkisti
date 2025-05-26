-- extra_queries.sql
-- 💡 Additional SQL practice queries using PostgreSQL

-- 🧮 Aggregate Functions

-- Average product price
SELECT AVG(price) AS average_price FROM products;

-- Most expensive product
SELECT name, price FROM products ORDER BY price DESC LIMIT 1;

-- Least stocked product
SELECT name, stock FROM products ORDER BY stock ASC LIMIT 1;

-- 📋 Pagination (LIMIT & OFFSET)

-- First 2 users
SELECT * FROM users LIMIT 2;

-- Next 2 users after skipping the first 2
SELECT * FROM users OFFSET 2 LIMIT 2;

-- 🧠 Conditional CASE WHEN

-- Friendly order status
SELECT o.id,
       u.email,
       CASE
           WHEN o.status = 'pending' THEN '⏳ In Progress'
           WHEN o.status = 'completed' THEN '✅ Done'
           WHEN o.status = 'canceled' THEN '❌ Canceled'
           ELSE 'Unknown'
           END AS readable_status
FROM orders o
         JOIN users u ON u.id = o.user_id;

-- 🔗 Self-Join (Users with same password)
SELECT u1.email AS user1, u2.email AS user2, u1.password
FROM users u1
         JOIN users u2 ON u1.password = u2.password AND u1.id < u2.id;

-- 🔎 IN / NOT IN

-- Users who placed orders
SELECT * FROM users
WHERE id IN (SELECT user_id FROM orders);

-- Users who never placed an order
SELECT * FROM users
WHERE id NOT IN (SELECT user_id FROM orders);

-- 🧼 Aggregation per user (completed orders only)
SELECT u.email,
       SUM(p.price * ci.quantity) AS total_spent
FROM users u
         JOIN orders o ON u.id = o.user_id
         JOIN cart_items ci ON o.id = ci.order_id
         JOIN products p ON ci.product_id = p.id
WHERE o.status = 'completed'
GROUP BY u.email;

-- 🧠 Window Function: Rank products by price
SELECT name, price,
       RANK() OVER (ORDER BY price DESC) AS price_rank
FROM products;
