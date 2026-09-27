DROP DATABASE IF EXISTS Agro_Shop_db;
CREATE DATABASE Agro_Shop_db;

show databases;
USE Agro_Shop_db;


-- ----------------------------------------------------------------------------
-- STUDENT 1 (ID: IT22000001) - User Account & Customer Profile Management
-- ----------------------------------------------------------------------------

-- Table 1: users (Student 1: IT22000001)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'EMPLOYEE', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: customer (Student 1: IT22000001)
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_num VARCHAR(15) NOT NULL,
    address VARCHAR(255) NOT NULL,
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- STUDENT 2 (ID: IT22000002) - Employee Management & Audit Logs
-- ----------------------------------------------------------------------------

-- Table 3: employees (Student 2: IT22000002)
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    position VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Table 4: system_logs (Student 2: IT22000002)
CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    log_level ENUM('INFO', 'WARNING', 'ERROR') DEFAULT 'INFO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------------------
-- STUDENT 3 (ID: IT22000003) - Catalog, Products & Inventory Control
-- ----------------------------------------------------------------------------

-- Table 5: categories (Student 3: IT22000003)
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    status BOOLEAN DEFAULT TRUE
);

-- Table 6: products (Student 3: IT22000003)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    image_url VARCHAR(500),
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT chk_product_price CHECK (price >= 0)
);

-- Table 7: inventory (Student 3: IT22000003)
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    minimum_stock DECIMAL(10,2) NOT NULL DEFAULT 5.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_qty CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_min_stock CHECK (minimum_stock >= 0)
);

-- ----------------------------------------------------------------------------
-- STUDENT 4 (ID: IT22000004) - Procurement, Orders & Payments Processing
-- ----------------------------------------------------------------------------

-- Table 8: suppliers (Student 4: IT22000004)
CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE,
    address VARCHAR(255),
    status BOOLEAN DEFAULT TRUE
);

-- Table 9: purchase_orders (Student 4: IT22000004)
CREATE TABLE purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    status ENUM('PENDING', 'APPROVED', 'RECEIVED', 'CANCELLED') DEFAULT 'PENDING',
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    CONSTRAINT chk_purchase_total CHECK (total_amount >= 0)
);

-- Table 10: purchase_items (Student 4: IT22000004)
CREATE TABLE purchase_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_purchase_item_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_pi_qty CHECK (quantity > 0),
    CONSTRAINT chk_pi_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_pi_subtotal CHECK (subtotal >= 0)
);

-- Table 11: orders (Student 4: IT22000004)
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

-- Table 12: order_items (Student 4: IT22000004)
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_oi_qty CHECK (quantity > 0),
    CONSTRAINT chk_oi_price CHECK (unit_price >= 0),
    CONSTRAINT chk_oi_subtotal CHECK (subtotal >= 0)
);

-- Table 13: payments (Student 4: IT22000004)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);


-- ============================================================================
-- TASK 3(b): POPULATING THE DATABASE (Data Population)
-- ============================================================================

-- Student 1 (IT22000001): Users & Customers
INSERT INTO users (id, username, password, email, role, status) VALUES
(1, 'admin_user', 'hash_admin123', 'admin@agroshop.lk', 'ADMIN', TRUE),
(2, 'saman_emp', 'hash_emp123', 'saman@agroshop.lk', 'EMPLOYEE', TRUE),
(3, 'kaveesha_p', 'hash_cust1', 'kaveesha@gmail.com', 'CUSTOMER', TRUE),
(4, 'nimal_s', 'hash_cust2', 'nimal@gmail.com', 'CUSTOMER', TRUE),
(5, 'sunil_f', 'hash_cust3', 'sunil@gmail.com', 'CUSTOMER', TRUE);

INSERT INTO customer (id, user_id, first_name, last_name, email, phone_num, address) VALUES
(1, 3, 'Kaveesha', 'Perera', 'kaveesha@gmail.com', '0771234567', 'No 17, Kotikawatta, Colombo'),
(2, 4, 'Nimal', 'Silva', 'nimal@gmail.com', '0719876543', 'Main Street, Kandy'),
(3, 5, 'Sunil', 'Fernando', 'sunil@gmail.com', '0751122334', 'Station Road, Kurunegala');

-- Student 2 (IT22000002): Employees & Logs
INSERT INTO employees (id, user_id, first_name, last_name, phone, position) VALUES
(1, 2, 'Saman', 'Kumara', '0781112223', 'Store Manager');

INSERT INTO system_logs (id, user_id, action, description, log_level) VALUES
(1, 1, 'USER_LOGIN', 'Admin user logged in successfully', 'INFO'),
(2, 2, 'UPDATE_STOCK', 'Saman updated stock levels for seeds', 'INFO'),
(3, 3, 'FAILED_LOGIN', 'Customer entered wrong password twice', 'WARNING');

-- Student 3 (IT22000003): Categories, Products & Inventory
INSERT INTO categories (id, name, description) VALUES
(1, 'Vegetables', 'Fresh agricultural organic vegetables'),
(2, 'Fruits', 'Fresh local fruits'),
(3, 'Seeds', 'High yield agricultural seeds'),
(4, 'Fertilizer', 'Soil organic and chemical fertilizers'),
(5, 'Equipment', 'Farming tools and hardware');

INSERT INTO products (id, category_id, name, description, price, unit) VALUES
(1, 1, 'Fresh Tomatoes', 'Organic farm tomatoes', 250.00, '1 kg'),
(2, 1, 'Organic Carrots', 'Fresh Nuwara Eliya carrots', 300.00, '1 kg'),
(3, 3, 'Tomato Seeds', 'High yield hybrid tomato seed pack', 100.00, '1 pack'),
(4, 3, 'Rice Seeds', 'BG358 Paddy seeds 50kg bag', 8500.00, '50 kg bag'),
(5, 4, 'Organic Compost', 'Nutrient rich compost fertilizer', 450.00, '5 kg pack');

INSERT INTO inventory (id, product_id, quantity, minimum_stock) VALUES
(1, 1, 120.00, 20.00),
(2, 2, 80.00, 15.00),
(3, 3, 15.00, 30.00), -- Low stock condition
(4, 4, 50.00, 10.00),
(5, 5, 200.00, 25.00);

-- Student 4 (IT22000004): Suppliers, Procurement, Orders & Payments
INSERT INTO suppliers (id, name, phone, email, address) VALUES
(1, 'Lanka Agro Supplies', '0112334455', 'sales@lankaagro.lk', 'Industrial Zone, Biyagama'),
(2, 'CIC Seeds Pvt Ltd', '0115667788', 'info@cicseeds.lk', 'Lotus Road, Colombo 01');

INSERT INTO purchase_orders (id, supplier_id, total_amount, status) VALUES
(1, 1, 45000.00, 'RECEIVED'),
(2, 2, 12000.00, 'APPROVED');

INSERT INTO purchase_items (id, purchase_order_id, product_id, quantity, unit_cost, subtotal) VALUES
(1, 1, 5, 100.00, 400.00, 40000.00),
(2, 2, 3, 120.00, 80.00, 9600.00);

INSERT INTO orders (id, customer_id, total_amount, status) VALUES
(1, 1, 800.00, 'DELIVERED'),
(2, 2, 8500.00, 'PROCESSING'),
(3, 3, 550.00, 'PENDING');

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 2.00, 250.00, 500.00),
(2, 1, 2, 1.00, 300.00, 300.00),
(3, 2, 4, 1.00, 8500.00, 8500.00),
(4, 3, 3, 1.00, 100.00, 100.00),
(5, 3, 5, 1.00, 450.00, 450.00);

INSERT INTO payments (id, order_id, amount, payment_method, status) VALUES
(1, 1, 800.00, 'CREDIT_CARD', 'COMPLETED'),
(2, 2, 8500.00, 'BANK_TRANSFER', 'COMPLETED'),
(3, 3, 550.00, 'CASH_ON_DELIVERY', 'PENDING');


-- ============================================================================
-- TASK 3(c): SQL QUERIES (Minimum 4 Queries per Student)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- STUDENT 1 QUERIES (ID: IT22000001)
-- ----------------------------------------------------------------------------

-- Query 1.1: Customer Profiles with User Login Details (INNER JOIN)
SELECT c.id AS customer_id, CONCAT(c.first_name, ' ', c.last_name) AS full_name, u.username, u.email, c.phone_num, c.address 
FROM customer c
JOIN users u ON c.user_id = u.id;

-- Query 1.2: Customers who have not placed any orders yet (LEFT JOIN + NULL Check)
SELECT c.id, c.first_name, c.last_name, c.email 
FROM customer c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.id IS NULL;

-- Query 1.3: Count registered customers grouped by area/city (AGGREGATION)
SELECT address, COUNT(*) AS customer_count 
FROM customer 
GROUP BY address;

-- Query 1.4: Update customer contact info safely
UPDATE customer 
SET phone_num = '0779998877', address = 'No 45, Malabe, Colombo' 
WHERE id = 1;


-- ----------------------------------------------------------------------------
-- STUDENT 2 QUERIES (ID: IT22000002)
-- ----------------------------------------------------------------------------

-- Query 2.1: Audit Logs with User Context (INNER JOIN)
SELECT l.id AS log_id, u.username, u.role, l.action, l.description, l.log_level, l.created_at 
FROM system_logs l
JOIN users u ON l.user_id = u.id
ORDER BY l.created_at DESC;

-- Query 2.2: Log Activity Count Grouped by Log Level (GROUP BY)
SELECT log_level, COUNT(*) AS total_logs 
FROM system_logs 
GROUP BY log_level;

-- Query 2.3: Retrieve Warning & Error Logs (FILTERING)
SELECT * FROM system_logs 
WHERE log_level IN ('WARNING', 'ERROR');

-- Query 2.4: Employee profiles linked with user details
SELECT e.id, e.first_name, e.last_name, e.position, u.email, u.role 
FROM employees e
JOIN users u ON e.user_id = u.id;


-- ----------------------------------------------------------------------------
-- STUDENT 3 QUERIES (ID: IT22000003)
-- ----------------------------------------------------------------------------

-- Query 3.1: Low Stock Alert Query (Quantity <= Minimum Stock)
SELECT p.id, p.name AS product_name, c.name AS category, i.quantity, i.minimum_stock 
FROM inventory i
JOIN products p ON i.product_id = p.id
JOIN categories c ON p.category_id = c.id
WHERE i.quantity <= i.minimum_stock;

-- Query 3.2: Average Product Price and Count per Category (AGGREGATION & JOIN)
SELECT c.name AS category_name, COUNT(p.id) AS total_products, AVG(p.price) AS avg_price 
FROM categories c
LEFT JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.name;

-- Query 3.3: Categories containing more than 1 product (HAVING Clause)
SELECT c.name, COUNT(p.id) AS product_count 
FROM categories c
JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.name
HAVING COUNT(p.id) > 1;

-- Query 3.4: Create Active Product Catalog View (DATABASE VIEW)
CREATE VIEW active_product_catalog AS
SELECT p.id, p.name AS product_name, c.name AS category_name, p.price, p.unit, i.quantity 
FROM products p
JOIN categories c ON p.category_id = c.id
JOIN inventory i ON p.id = i.product_id
WHERE p.status = TRUE;

-- Display View
SELECT * FROM active_product_catalog;


-- ----------------------------------------------------------------------------
-- STUDENT 4 QUERIES (ID: IT22000004)
-- ----------------------------------------------------------------------------

-- Query 4.1: Monthly Revenue Summary from Completed Payments (AGGREGATION & DATE FORMAT)
SELECT DATE_FORMAT(payment_date, '%Y-%m') AS sales_month, SUM(amount) AS total_revenue 
FROM payments 
WHERE status = 'COMPLETED'
GROUP BY sales_month;

-- Query 4.2: Top Best-Selling Products (JOIN, GROUP BY, ORDER BY, LIMIT)
SELECT p.name AS product_name, SUM(oi.quantity) AS total_quantity_sold, SUM(oi.subtotal) AS total_sales 
FROM order_items oi
JOIN products p ON oi.product_id = p.id
GROUP BY p.id, p.name
ORDER BY total_quantity_sold DESC 
LIMIT 3;

-- Query 4.3: Detailed Order Invoice Breakdown (MULTI-JOIN)
SELECT o.id AS order_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, 
       o.order_date, o.total_amount, p.payment_method, p.status AS payment_status
FROM orders o
JOIN customer c ON o.customer_id = c.id
JOIN payments p ON o.id = p.order_id;

-- Query 4.4: High Value Customers who spent over LKR 1,000 (SUBQUERY)
SELECT c.id, c.first_name, c.last_name, c.email 
FROM customer c
WHERE c.id IN (
    SELECT customer_id 
    FROM orders 
    GROUP BY customer_id 
    HAVING SUM(total_amount) > 1000.00
);