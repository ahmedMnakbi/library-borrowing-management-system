USE library_db;

DELETE FROM reservations;
DELETE FROM fines;
DELETE FROM loans;

INSERT INTO users (username, password_hash, role, first_name, last_name, email, phone, active)
VALUES
    ('admin1', 'YWRtaW4tc2FsdDAwMDAwMA==:s0U/WaBzaqKf2M4fl5Z93VXqS5tXfMgmNbiT114s5VU=', 'ADMIN', 'Ali', 'Admin', 'admin@test.com', '12345678', TRUE),
    ('lib1', 'bGliLXNhbHQwMDAwMDAwMA==:i2TyFaiVlZYyWb/YD0oiyeM3lNzUGDKpg1ZQMNzGlnE=', 'LIBRARIAN', 'Lina', 'Bib', 'lib1@test.com', '22222222', TRUE),
    ('mem1', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Sami', 'Member', 'mem1@test.com', '555111', TRUE)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    active = TRUE;

INSERT INTO staff (staff_id, employee_number, hire_date)
SELECT user_id, 'EMP001', CURDATE()
FROM users
WHERE username = 'admin1'
ON DUPLICATE KEY UPDATE employee_number = VALUES(employee_number);

INSERT INTO staff (staff_id, employee_number, hire_date)
SELECT user_id, 'EMP002', CURDATE()
FROM users
WHERE username = 'lib1'
ON DUPLICATE KEY UPDATE employee_number = VALUES(employee_number);

INSERT INTO members (member_id, membership_number, address, registration_date, max_loans)
SELECT user_id, 'M001', 'Tunis', CURDATE(), 3
FROM users
WHERE username = 'mem1'
ON DUPLICATE KEY UPDATE membership_number = VALUES(membership_number);

INSERT INTO categories (name, description)
VALUES ('Informatique', 'Livres info')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO authors (first_name, last_name)
SELECT 'Robert', 'Martin'
WHERE NOT EXISTS (
    SELECT 1 FROM authors WHERE first_name = 'Robert' AND last_name = 'Martin'
);

INSERT INTO books (isbn, title, publisher, publication_year, category_id, active)
SELECT '9780132350884', 'Clean Code', 'Prentice Hall', 2008, category_id, TRUE
FROM categories
WHERE name = 'Informatique'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    publisher = VALUES(publisher),
    publication_year = VALUES(publication_year),
    category_id = VALUES(category_id),
    active = TRUE;

INSERT IGNORE INTO book_authors (book_id, author_id)
SELECT b.book_id, a.author_id
FROM books b
JOIN authors a ON a.first_name = 'Robert' AND a.last_name = 'Martin'
WHERE b.isbn = '9780132350884';

INSERT INTO book_copies (book_id, barcode, status, acquisition_date)
SELECT book_id, 'BC001', 'AVAILABLE', CURDATE()
FROM books
WHERE isbn = '9780132350884'
ON DUPLICATE KEY UPDATE status = 'AVAILABLE';

INSERT INTO book_copies (book_id, barcode, status, acquisition_date)
SELECT book_id, 'BC002', 'AVAILABLE', CURDATE()
FROM books
WHERE isbn = '9780132350884'
ON DUPLICATE KEY UPDATE status = 'AVAILABLE';
