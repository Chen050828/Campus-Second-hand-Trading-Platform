-- Campus Second-hand Market Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS campus_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_market;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    city VARCHAR(50),
    gender VARCHAR(10),
    bank_account VARCHAR(16),
    business_license_img VARCHAR(255),
    id_card_img VARCHAR(255),
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    merchant_level INT DEFAULT NULL,
    store_name VARCHAR(100),
    service_rating DOUBLE DEFAULT 5.0,
    service_rating_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    original_price DOUBLE NOT NULL,
    discount_price DOUBLE NOT NULL,
    size VARCHAR(100),
    images TEXT,
    usage_notes TEXT,
    allow_bargain BIT DEFAULT 0,
    quantity INT NOT NULL,
    condition_ VARCHAR(20),
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    sales_count INT DEFAULT 0,
    avg_rating DOUBLE DEFAULT 0.0,
    review_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Cart items
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_price DOUBLE NOT NULL,
    platform_fee DOUBLE DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    meet_location VARCHAR(255),
    meet_time DATETIME,
    receive_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT NOT NULL,
    content TEXT,
    type VARCHAR(10) NOT NULL DEFAULT 'PRODUCT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Wallets
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    points INT NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Wallet Transactions
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    related_order_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

-- Return Requests
CREATE TABLE IF NOT EXISTS return_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    merchant_reply TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- System Account (intermediate fund holding)
CREATE TABLE IF NOT EXISTS system_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    total_fee DOUBLE NOT NULL DEFAULT 0.0
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'ADMIN', 'APPROVED');

-- Insert default categories
INSERT INTO categories (name, description) VALUES
('教材教辅', '教材、参考书、考试资料等'),
('电子数码', '手机、电脑、平板、配件等'),
('生活用品', '日常用品、小家电、寝室用品等'),
('服饰鞋包', '衣服、鞋子、包包、配饰等'),
('运动户外', '运动器材、户外装备等'),
('美妆护肤', '化妆品、护肤品、个人护理等'),
('食品饮料', '零食、饮料、特产等'),
('其他', '其他分类');
