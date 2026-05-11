CREATE DATABASE IF NOT EXISTS library_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE library_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM ('ADMIN', 'LIBRARIAN', 'MEMBER') NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) UNIQUE,
    phone VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS members (
    member_id INT PRIMARY KEY,
    membership_number VARCHAR(30) NOT NULL UNIQUE,
    address VARCHAR(255),
    registration_date DATE NOT NULL,
    max_loans INT NOT NULL DEFAULT 3,
    FOREIGN KEY (member_id) REFERENCES users (user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS staff (
    staff_id INT PRIMARY KEY,
    employee_number VARCHAR(30) NOT NULL UNIQUE,
    hire_date DATE,
    FOREIGN KEY (staff_id) REFERENCES users (user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS authors (
    author_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(150) NOT NULL,
    publisher VARCHAR(120),
    publication_year INT,
    category_id INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books (book_id)
        ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors (author_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS book_copies (
    copy_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    barcode VARCHAR(50) NOT NULL UNIQUE,
    status ENUM ('AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'DAMAGED')
        NOT NULL DEFAULT 'AVAILABLE',
    acquisition_date DATE,
    FOREIGN KEY (book_id) REFERENCES books (book_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS loans (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    copy_id INT NOT NULL,
    member_id INT NOT NULL,
    librarian_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM ('ONGOING', 'RETURNED', 'OVERDUE', 'LOST')
        NOT NULL DEFAULT 'ONGOING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id),
    FOREIGN KEY (member_id) REFERENCES members (member_id),
    FOREIGN KEY (librarian_id) REFERENCES staff (staff_id)
);

CREATE TABLE IF NOT EXISTS fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(255),
    status ENUM ('UNPAID', 'PAID', 'CANCELLED')
        NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    FOREIGN KEY (loan_id) REFERENCES loans (loan_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date DATE,
    status ENUM ('PENDING', 'FULFILLED', 'CANCELLED', 'EXPIRED')
        NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (book_id) REFERENCES books (book_id),
    FOREIGN KEY (member_id) REFERENCES members (member_id)
);

