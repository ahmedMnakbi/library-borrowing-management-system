USE library_db;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM reservations;
DELETE FROM fines;
DELETE FROM loans;
DELETE FROM book_copies;
DELETE FROM book_authors;
DELETE FROM books;
DELETE FROM authors;
DELETE FROM categories;
DELETE FROM members;
DELETE FROM staff;
DELETE FROM users;

ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE fines AUTO_INCREMENT = 1;
ALTER TABLE loans AUTO_INCREMENT = 1;
ALTER TABLE book_copies AUTO_INCREMENT = 1;
ALTER TABLE books AUTO_INCREMENT = 1;
ALTER TABLE authors AUTO_INCREMENT = 1;
ALTER TABLE categories AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (username, password_hash, role, first_name, last_name, email, phone, active)
VALUES
    ('admin1', 'YWRtaW4tc2FsdDAwMDAwMA==:s0U/WaBzaqKf2M4fl5Z93VXqS5tXfMgmNbiT114s5VU=', 'ADMIN', 'Ali', 'Admin', 'admin@test.com', '12345678', TRUE),
    ('lib1', 'bGliLXNhbHQwMDAwMDAwMA==:i2TyFaiVlZYyWb/YD0oiyeM3lNzUGDKpg1ZQMNzGlnE=', 'LIBRARIAN', 'Lina', 'Ben Salah', 'lib1@test.com', '22222222', TRUE),
    ('lib2', 'bGliLXNhbHQwMDAwMDAwMA==:i2TyFaiVlZYyWb/YD0oiyeM3lNzUGDKpg1ZQMNzGlnE=', 'LIBRARIAN', 'Karim', 'Mansour', 'lib2@test.com', '22223333', TRUE),
    ('mem1', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Sami', 'Member', 'mem1@test.com', '555111', TRUE),
    ('mem2', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Nour', 'Trabelsi', 'mem2@test.com', '555222', TRUE),
    ('mem3', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Yassine', 'Gharbi', 'mem3@test.com', '555333', TRUE),
    ('mem4', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Amira', 'Saidi', 'mem4@test.com', '555444', TRUE),
    ('mem5', 'bWVtLXNhbHQwMDAwMDAwMA==:KeXlGHy9j6hJXQdvi7gPz/DQRkFgkHdoqdOY9J7Zd08=', 'MEMBER', 'Mehdi', 'Kacem', 'mem5@test.com', '555555', TRUE);

INSERT INTO staff (staff_id, employee_number, hire_date)
VALUES
    (1, 'EMP001', DATE_SUB(CURDATE(), INTERVAL 900 DAY)),
    (2, 'EMP002', DATE_SUB(CURDATE(), INTERVAL 620 DAY)),
    (3, 'EMP003', DATE_SUB(CURDATE(), INTERVAL 240 DAY));

INSERT INTO members (member_id, membership_number, address, registration_date, max_loans)
VALUES
    (4, 'M001', 'Tunis', DATE_SUB(CURDATE(), INTERVAL 400 DAY), 3),
    (5, 'M002', 'Ariana', DATE_SUB(CURDATE(), INTERVAL 300 DAY), 3),
    (6, 'M003', 'Sousse', DATE_SUB(CURDATE(), INTERVAL 180 DAY), 3),
    (7, 'M004', 'Sfax', DATE_SUB(CURDATE(), INTERVAL 120 DAY), 3),
    (8, 'M005', 'Nabeul', DATE_SUB(CURDATE(), INTERVAL 60 DAY), 3);

INSERT INTO categories (name, description)
VALUES
    ('Informatique', 'Programmation, architecture logicielle et bonnes pratiques'),
    ('Science', 'Sciences, technologies et anticipation'),
    ('Roman', 'Romans classiques et modernes'),
    ('Histoire', 'Histoire et sciences humaines'),
    ('Mathématiques', 'Algorithmes, logique et mathematiques appliquees'),
    ('Développement personnel', 'Habitudes, productivite et amelioration personnelle');

INSERT INTO authors (first_name, last_name)
VALUES
    ('Robert C.', 'Martin'),
    ('Joshua', 'Bloch'),
    ('James', 'Clear'),
    ('Yuval Noah', 'Harari'),
    ('George', 'Orwell'),
    ('Albert', 'Camus'),
    ('Isaac', 'Asimov'),
    ('Thomas H.', 'Cormen'),
    ('Andrew', 'Hunt'),
    ('David', 'Thomas'),
    ('Erich', 'Gamma');

INSERT INTO books (isbn, title, publisher, publication_year, category_id, active)
VALUES
    ('9780132350884', 'Clean Code', 'Prentice Hall', 2008, 1, TRUE),
    ('9780134685991', 'Effective Java', 'Addison-Wesley', 2018, 1, TRUE),
    ('9780262046305', 'Introduction to Algorithms', 'MIT Press', 2022, 5, TRUE),
    ('9780735211292', 'Atomic Habits', 'Avery', 2018, 6, TRUE),
    ('9780062316097', 'Sapiens', 'Harper', 2015, 4, TRUE),
    ('9780451524935', '1984', 'Signet Classics', 1950, 3, TRUE),
    ('9782070360024', 'L''Étranger', 'Gallimard', 1942, 3, TRUE),
    ('9780553293357', 'Foundation', 'Bantam', 1951, 2, TRUE),
    ('9780135957059', 'The Pragmatic Programmer', 'Addison-Wesley', 2019, 1, TRUE),
    ('9780201633610', 'Design Patterns', 'Addison-Wesley', 1994, 1, TRUE);

INSERT INTO book_authors (book_id, author_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 8),
    (4, 3),
    (5, 4),
    (6, 5),
    (7, 6),
    (8, 7),
    (9, 9),
    (9, 10),
    (10, 11);

INSERT INTO book_copies (book_id, barcode, status, acquisition_date)
VALUES
    (1, 'BC001', 'BORROWED', DATE_SUB(CURDATE(), INTERVAL 500 DAY)),
    (1, 'BC002', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 500 DAY)),
    (2, 'BC003', 'BORROWED', DATE_SUB(CURDATE(), INTERVAL 320 DAY)),
    (2, 'BC004', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 320 DAY)),
    (3, 'BC005', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 250 DAY)),
    (4, 'BC006', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 210 DAY)),
    (5, 'BC007', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 200 DAY)),
    (6, 'BC008', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 180 DAY)),
    (7, 'BC009', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 160 DAY)),
    (8, 'BC010', 'BORROWED', DATE_SUB(CURDATE(), INTERVAL 140 DAY)),
    (9, 'BC011', 'AVAILABLE', DATE_SUB(CURDATE(), INTERVAL 120 DAY)),
    (10, 'BC012', 'RESERVED', DATE_SUB(CURDATE(), INTERVAL 100 DAY));

INSERT INTO loans (copy_id, member_id, librarian_id, borrow_date, due_date, return_date, status)
VALUES
    (1, 5, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), NULL, 'ONGOING'),
    (3, 6, 2, DATE_SUB(CURDATE(), INTERVAL 9 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), NULL, 'ONGOING'),
    (10, 7, 3, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL, 'OVERDUE'),
    (7, 4, 3, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'RETURNED'),
    (9, 8, 2, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'RETURNED');

INSERT INTO fines (loan_id, amount, reason, status, created_at, paid_at)
VALUES
    (3, 22.00, 'Retard de 11 jour(s)', 'UNPAID', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
    (4, 8.00, 'Retard de 4 jour(s)', 'PAID', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY));

INSERT INTO reservations (book_id, member_id, reservation_date, expiry_date, status)
VALUES
    (10, 8, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'PENDING'),
    (6, 5, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'CANCELLED');
