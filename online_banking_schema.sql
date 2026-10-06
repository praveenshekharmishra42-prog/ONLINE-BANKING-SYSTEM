-- =====================================================================
-- Online Banking System - database setup
-- Run this whole script once in MySQL Workbench (File > Open SQL Script,
-- then click the lightning Execute button).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS online_banking;
USE online_banking;

-- Login accounts for both admins and customers.
-- Passwords are stored as SHA-256(salt + password), never as plain text.
CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(20)  NOT NULL UNIQUE,
    password_hash  CHAR(64)     NOT NULL,
    salt           CHAR(32)     NOT NULL,
    role           ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    full_name      VARCHAR(60)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Personal details of customers (one row per customer user).
CREATE TABLE IF NOT EXISTS customers (
    customer_id  INT PRIMARY KEY,
    father_name  VARCHAR(60),
    dob          DATE,
    gender       VARCHAR(10),
    email        VARCHAR(80),
    phone        CHAR(10),
    address      VARCHAR(150),
    city         VARCHAR(40),
    state        VARCHAR(40),
    pincode      CHAR(6),
    occupation   VARCHAR(40),
    CONSTRAINT fk_customer_user FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- One bank account per customer. Money is DECIMAL (exact), never FLOAT.
CREATE TABLE IF NOT EXISTS accounts (
    account_no    VARCHAR(12)   PRIMARY KEY,
    customer_id   INT           NOT NULL UNIQUE,
    account_type  ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance       DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status        ENUM('PENDING', 'ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    opened_on     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Every money movement. Rows are only inserted, never updated.
CREATE TABLE IF NOT EXISTS transactions (
    txn_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no     VARCHAR(12)   NOT NULL,
    txn_type       ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST') NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    description    VARCHAR(150),
    txn_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_account FOREIGN KEY (account_no) REFERENCES accounts(account_no) ON DELETE CASCADE,
    INDEX idx_txn_account_time (account_no, txn_time)
);

-- Default admin account ->  username: admin   password: admin123
INSERT IGNORE INTO users (username, password_hash, salt, role, full_name)
VALUES ('admin', '57a18c047e86c842b92cd0900a147199e2815bb3b01e2481f2f39bc49795e7c0', '4f1c2a9b7e3d5c8a6b0e9f2d1a7c4b3e', 'ADMIN', 'Bank Manager');

-- Application database user (used by db.properties)
CREATE USER IF NOT EXISTS 'bankuser'@'localhost' IDENTIFIED BY 'bank123';
GRANT SELECT, INSERT, UPDATE, DELETE ON online_banking.* TO 'bankuser'@'localhost';
FLUSH PRIVILEGES;
