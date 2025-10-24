DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id IDENTITY PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    status VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE accounts (
    id IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'INVESTMENT', 'CHEQUE')),
    branch VARCHAR(100),
    balance DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE transactions (
    id IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    amount DECIMAL(15,2) CHECK (amount > 0),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_customer_username ON customers(username);
CREATE INDEX idx_account_number ON accounts(account_number);

INSERT INTO customers (full_name, username, password_hash, email, role, status)
VALUES ('System Administrator', 'admin', 'admin123', 'admin@bank.com', 'ADMIN', 'APPROVED');
