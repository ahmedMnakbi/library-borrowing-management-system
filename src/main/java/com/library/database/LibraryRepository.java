package com.library.database;

import com.library.enums.CopyStatus;
import com.library.enums.FineStatus;
import com.library.enums.LoanStatus;
import com.library.enums.ReservationStatus;
import com.library.enums.Role;
import com.library.model.Admin;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Category;
import com.library.model.Fine;
import com.library.model.Librarian;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.Staff;
import com.library.model.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryRepository {

    // ===== users =====

    public Optional<User> findUserByUsername(String username) throws SQLException {
        String sql = """
                SELECT u.*, m.membership_number, m.address, m.registration_date, m.max_loans,
                       s.employee_number, s.hire_date
                FROM users u
                LEFT JOIN members m ON u.user_id = m.member_id
                LEFT JOIN staff s ON u.user_id = s.staff_id
                WHERE u.username = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<User> findUserById(int userId) throws SQLException {
        String sql = """
                SELECT u.*, m.membership_number, m.address, m.registration_date, m.max_loans,
                       s.employee_number, s.hire_date
                FROM users u
                LEFT JOIN members m ON u.user_id = m.member_id
                LEFT JOIN staff s ON u.user_id = s.staff_id
                WHERE u.user_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                User user = mapUser(resultSet);
                if (user instanceof Member member) {
                    enrichMember(connection, member);
                }
                return Optional.of(user);
            }
        }
    }

    public List<User> listUsers() throws SQLException {
        String sql = """
                SELECT u.*, m.membership_number, m.address, m.registration_date, m.max_loans,
                       s.employee_number, s.hire_date
                FROM users u
                LEFT JOIN members m ON u.user_id = m.member_id
                LEFT JOIN staff s ON u.user_id = s.staff_id
                ORDER BY u.user_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        }
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        String like = "%" + keyword + "%";
        String sql = """
                SELECT u.*, m.membership_number, m.address, m.registration_date, m.max_loans,
                       s.employee_number, s.hire_date
                FROM users u
                LEFT JOIN members m ON u.user_id = m.member_id
                LEFT JOIN staff s ON u.user_id = s.staff_id
                WHERE u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?
                ORDER BY u.user_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, like);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        }
    }

    public Staff saveStaff(Staff staff) throws SQLException {
        String userSql = """
                INSERT INTO users (username, password_hash, role, first_name, last_name, email, phone, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String staffSql = "INSERT INTO staff (staff_id, employee_number, hire_date) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement staffStatement = connection.prepareStatement(staffSql)) {
                fillUserStatement(userStatement, staff);
                userStatement.executeUpdate();
                staff.setId(readGeneratedId(userStatement));

                staffStatement.setInt(1, staff.getId());
                staffStatement.setString(2, staff.getEmployeeNumber());
                setDate(staffStatement, 3, staff.getHireDate());
                staffStatement.executeUpdate();
                connection.commit();
                return staff;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Member saveMember(Member member) throws SQLException {
        String userSql = """
                INSERT INTO users (username, password_hash, role, first_name, last_name, email, phone, active)
                VALUES (?, ?, 'MEMBER', ?, ?, ?, ?, ?)
                """;
        String memberSql = """
                INSERT INTO members (member_id, membership_number, address, registration_date, max_loans)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement memberStatement = connection.prepareStatement(memberSql)) {
                userStatement.setString(1, member.getUsername());
                userStatement.setString(2, member.getPasswordHash());
                userStatement.setString(3, member.getFirstName());
                userStatement.setString(4, member.getLastName());
                userStatement.setString(5, member.getEmail());
                userStatement.setString(6, member.getPhone());
                userStatement.setBoolean(7, member.isActive());
                userStatement.executeUpdate();
                member.setId(readGeneratedId(userStatement));

                memberStatement.setInt(1, member.getId());
                memberStatement.setString(2, member.getMembershipNumber());
                memberStatement.setString(3, member.getAddress());
                setDate(memberStatement, 4, member.getRegistrationDate());
                memberStatement.setInt(5, member.getMaxLoans());
                memberStatement.executeUpdate();
                connection.commit();
                return member;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateUser(User user) throws SQLException {
        String sql = """
                UPDATE users
                SET username = ?, first_name = ?, last_name = ?, email = ?, phone = ?, active = ?
                WHERE user_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setBoolean(6, user.isActive());
            statement.setInt(7, user.getId());
            statement.executeUpdate();
        }
    }

    public void setUserActive(int userId, boolean active) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET active = ? WHERE user_id = ?")) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    // ===== books and copies =====

    public Category saveCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();
            category.setCategoryId(readGeneratedId(statement));
            return category;
        }
    }

    public Author saveAuthor(Author author) throws SQLException {
        String sql = "INSERT INTO authors (first_name, last_name) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            statement.executeUpdate();
            author.setAuthorId(readGeneratedId(statement));
            return author;
        }
    }

    public List<Category> listCategories() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM categories ORDER BY name");
             ResultSet resultSet = statement.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }
            return categories;
        }
    }

    public List<Author> listAuthors() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM authors ORDER BY last_name, first_name");
             ResultSet resultSet = statement.executeQuery()) {
            List<Author> authors = new ArrayList<>();
            while (resultSet.next()) {
                authors.add(mapAuthor(resultSet));
            }
            return authors;
        }
    }

    public Book saveBook(Book book) throws SQLException {
        String bookSql = """
                INSERT INTO books (isbn, title, publisher, publication_year, category_id, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String authorSql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement bookStatement = connection.prepareStatement(bookSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement authorStatement = connection.prepareStatement(authorSql)) {
                fillBookStatement(bookStatement, book);
                bookStatement.executeUpdate();
                book.setBookId(readGeneratedId(bookStatement));

                for (Author author : book.getAuthors()) {
                    authorStatement.setInt(1, book.getBookId());
                    authorStatement.setInt(2, author.getAuthorId());
                    authorStatement.addBatch();
                }
                authorStatement.executeBatch();
                connection.commit();
                return book;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Optional<Book> findBookById(int bookId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findBookById(connection, bookId);
        }
    }

    public List<Book> listBooks() throws SQLException {
        String sql = """
                SELECT b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON b.category_id = c.category_id
                WHERE b.active = TRUE
                ORDER BY b.title
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                Book book = mapBook(resultSet);
                book.setAuthors(listAuthorsForBook(connection, book.getBookId()));
                book.setCopies(listCopiesForBook(connection, book.getBookId()));
                books.add(book);
            }
            return books;
        }
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        String like = "%" + keyword + "%";
        String sql = """
                SELECT DISTINCT b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON b.category_id = c.category_id
                LEFT JOIN book_authors ba ON b.book_id = ba.book_id
                LEFT JOIN authors a ON ba.author_id = a.author_id
                WHERE b.active = TRUE
                  AND (b.title LIKE ? OR b.isbn LIKE ? OR a.first_name LIKE ? OR a.last_name LIKE ?)
                ORDER BY b.title
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, like);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (resultSet.next()) {
                    Book book = mapBook(resultSet);
                    book.setAuthors(listAuthorsForBook(connection, book.getBookId()));
                    book.setCopies(listCopiesForBook(connection, book.getBookId()));
                    books.add(book);
                }
                return books;
            }
        }
    }

    public BookCopy saveCopy(BookCopy copy) throws SQLException {
        String sql = "INSERT INTO book_copies (book_id, barcode, status, acquisition_date) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, copy.getBook().getBookId());
            statement.setString(2, copy.getBarcode());
            statement.setString(3, copy.getStatus().name());
            setDate(statement, 4, copy.getAcquisitionDate());
            statement.executeUpdate();
            copy.setCopyId(readGeneratedId(statement));
            return copy;
        }
    }

    public List<BookCopy> listCopies() throws SQLException {
        String sql = """
                SELECT bc.*, b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                LEFT JOIN categories c ON b.category_id = c.category_id
                ORDER BY bc.copy_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<BookCopy> copies = new ArrayList<>();
            while (resultSet.next()) {
                copies.add(mapCopy(resultSet, mapBook(resultSet)));
            }
            return copies;
        }
    }

    public Optional<BookCopy> findCopyById(int copyId) throws SQLException {
        String sql = """
                SELECT bc.*, b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM book_copies bc
                JOIN books b ON bc.book_id = b.book_id
                LEFT JOIN categories c ON b.category_id = c.category_id
                WHERE bc.copy_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, copyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCopy(resultSet, mapBook(resultSet))) : Optional.empty();
            }
        }
    }

    public boolean hasAvailableCopy(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    public void updateCopyStatus(int copyId, CopyStatus status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            updateCopyStatus(connection, copyId, status);
        }
    }

    // ===== loans =====

    public Loan createLoan(int memberId, int copyId, int librarianId, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        String sql = """
                INSERT INTO loans (copy_id, member_id, librarian_id, borrow_date, due_date, status)
                VALUES (?, ?, ?, ?, ?, 'ONGOING')
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, copyId);
                statement.setInt(2, memberId);
                statement.setInt(3, librarianId);
                setDate(statement, 4, borrowDate);
                setDate(statement, 5, dueDate);
                statement.executeUpdate();
                int loanId = readGeneratedId(statement);
                updateCopyStatus(connection, copyId, CopyStatus.BORROWED);
                connection.commit();
                return findLoanById(connection, loanId).orElseThrow();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Optional<Loan> findLoanById(int loanId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return findLoanById(connection, loanId);
        }
    }

    public List<Loan> listActiveLoans() throws SQLException {
        return listLoans("WHERE l.status = 'ONGOING'", 0);
    }

    public List<Loan> listMemberLoans(int memberId) throws SQLException {
        return listLoans("WHERE l.member_id = ?", memberId);
    }

    public int countActiveLoansByMember(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status = 'ONGOING'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    public void returnLoan(Loan loan, BigDecimal fineAmount, String fineReason) throws SQLException {
        String loanSql = "UPDATE loans SET return_date = ?, status = 'RETURNED' WHERE loan_id = ?";
        String fineSql = "INSERT INTO fines (loan_id, amount, reason, status) VALUES (?, ?, ?, 'UNPAID')";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement loanStatement = connection.prepareStatement(loanSql)) {
                setDate(loanStatement, 1, loan.getReturnDate());
                loanStatement.setInt(2, loan.getLoanId());
                loanStatement.executeUpdate();
                updateCopyStatus(connection, loan.getBookCopy().getCopyId(), CopyStatus.AVAILABLE);

                if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                    try (PreparedStatement fineStatement = connection.prepareStatement(fineSql)) {
                        fineStatement.setInt(1, loan.getLoanId());
                        fineStatement.setBigDecimal(2, fineAmount);
                        fineStatement.setString(3, fineReason);
                        fineStatement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // ===== fines =====

    public List<Fine> listUnpaidFines() throws SQLException {
        return listFines("WHERE f.status = 'UNPAID'", 0);
    }

    public List<Fine> listMemberFines(int memberId) throws SQLException {
        return listFines("WHERE l.member_id = ?", memberId);
    }

    public boolean hasUnpaidFines(int memberId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM fines f
                JOIN loans l ON f.loan_id = l.loan_id
                WHERE l.member_id = ? AND f.status = 'UNPAID'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    public void markFinePaid(int fineId) throws SQLException {
        String sql = "UPDATE fines SET status = 'PAID', paid_at = NOW() WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fineId);
            statement.executeUpdate();
        }
    }

    public void cancelFine(int fineId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE fines SET status = 'CANCELLED' WHERE fine_id = ?")) {
            statement.setInt(1, fineId);
            statement.executeUpdate();
        }
    }

    // ===== reservations =====

    public Reservation createReservation(int bookId, int memberId, LocalDate reservationDate, LocalDate expiryDate) throws SQLException {
        String sql = """
                INSERT INTO reservations (book_id, member_id, reservation_date, expiry_date, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, bookId);
            statement.setInt(2, memberId);
            setDate(statement, 3, reservationDate);
            setDate(statement, 4, expiryDate);
            statement.executeUpdate();
            int reservationId = readGeneratedId(statement);
            return findReservationById(reservationId).orElseThrow();
        }
    }

    public boolean hasPendingReservation(int memberId, int bookId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM reservations
                WHERE member_id = ? AND book_id = ? AND status = 'PENDING'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setInt(2, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    public List<Reservation> listReservations() throws SQLException {
        return listReservations("ORDER BY r.reservation_id", 0);
    }

    public List<Reservation> listMemberReservations(int memberId) throws SQLException {
        return listReservations("WHERE r.member_id = ? ORDER BY r.reservation_id", memberId);
    }

    public void cancelReservation(int reservationId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?")) {
            statement.setInt(1, reservationId);
            statement.executeUpdate();
        }
    }

    private Optional<Reservation> findReservationById(int reservationId) throws SQLException {
        List<Reservation> reservations = listReservations("WHERE r.reservation_id = ?", reservationId);
        return reservations.stream().findFirst();
    }

    private List<Reservation> listReservations(String condition, int parameter) throws SQLException {
        String sql = """
                SELECT r.*, b.*, c.category_id, c.name AS category_name, c.description AS category_description,
                       u.*, m.membership_number, m.address, m.registration_date, m.max_loans
                FROM reservations r
                JOIN books b ON r.book_id = b.book_id
                LEFT JOIN categories c ON b.category_id = c.category_id
                JOIN members m ON r.member_id = m.member_id
                JOIN users u ON m.member_id = u.user_id
                """ + condition;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter > 0) {
                statement.setInt(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
                return reservations;
            }
        }
    }

    private List<Loan> listLoans(String condition, int parameter) throws SQLException {
        String sql = loanSelectSql() + " " + condition + " ORDER BY l.loan_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter > 0) {
                statement.setInt(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (resultSet.next()) {
                    loans.add(mapLoan(resultSet));
                }
                return loans;
            }
        }
    }

    private List<Fine> listFines(String condition, int parameter) throws SQLException {
        String sql = "SELECT f.* FROM fines f JOIN loans l ON f.loan_id = l.loan_id " + condition + " ORDER BY f.fine_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter > 0) {
                statement.setInt(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Fine> fines = new ArrayList<>();
                while (resultSet.next()) {
                    Fine fine = new Fine();
                    fine.setFineId(resultSet.getInt("fine_id"));
                    fine.setAmount(resultSet.getBigDecimal("amount"));
                    fine.setReason(resultSet.getString("reason"));
                    fine.setStatus(FineStatus.valueOf(resultSet.getString("status")));
                    fine.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
                    fine.setPaidAt(toLocalDateTime(resultSet.getTimestamp("paid_at")));
                    fine.setLoan(findLoanById(resultSet.getInt("loan_id")).orElse(null));
                    fines.add(fine);
                }
                return fines;
            }
        }
    }

    private Optional<Loan> findLoanById(Connection connection, int loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(loanSelectSql() + " WHERE l.loan_id = ?")) {
            statement.setInt(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapLoan(resultSet)) : Optional.empty();
            }
        }
    }

    private String loanSelectSql() {
        return """
                SELECT l.*,
                       mu.user_id AS member_user_id, mu.username AS member_username, mu.password_hash AS member_password_hash,
                       mu.role AS member_role, mu.first_name AS member_first_name, mu.last_name AS member_last_name,
                       mu.email AS member_email, mu.phone AS member_phone, mu.active AS member_active,
                       m.membership_number, m.address, m.registration_date, m.max_loans,
                       su.user_id AS staff_user_id, su.username AS staff_username, su.password_hash AS staff_password_hash,
                       su.role AS staff_role, su.first_name AS staff_first_name, su.last_name AS staff_last_name,
                       su.email AS staff_email, su.phone AS staff_phone, su.active AS staff_active,
                       s.employee_number, s.hire_date,
                       bc.copy_id, bc.barcode, bc.status AS copy_status, bc.acquisition_date,
                       b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM loans l
                JOIN members m ON l.member_id = m.member_id
                JOIN users mu ON m.member_id = mu.user_id
                JOIN staff s ON l.librarian_id = s.staff_id
                JOIN users su ON s.staff_id = su.user_id
                JOIN book_copies bc ON l.copy_id = bc.copy_id
                JOIN books b ON bc.book_id = b.book_id
                LEFT JOIN categories c ON b.category_id = c.category_id
                """;
    }

    private Optional<Book> findBookById(Connection connection, int bookId) throws SQLException {
        String sql = """
                SELECT b.*, c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON b.category_id = c.category_id
                WHERE b.book_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Book book = mapBook(resultSet);
                book.setAuthors(listAuthorsForBook(connection, bookId));
                book.setCopies(listCopiesForBook(connection, bookId));
                return Optional.of(book);
            }
        }
    }

    private List<Author> listAuthorsForBook(Connection connection, int bookId) throws SQLException {
        String sql = """
                SELECT a.*
                FROM authors a
                JOIN book_authors ba ON a.author_id = ba.author_id
                WHERE ba.book_id = ?
                ORDER BY a.last_name, a.first_name
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Author> authors = new ArrayList<>();
                while (resultSet.next()) {
                    authors.add(mapAuthor(resultSet));
                }
                return authors;
            }
        }
    }

    private List<BookCopy> listCopiesForBook(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT * FROM book_copies WHERE book_id = ? ORDER BY copy_id";
        Book book = new Book();
        book.setBookId(bookId);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BookCopy> copies = new ArrayList<>();
                while (resultSet.next()) {
                    copies.add(mapCopy(resultSet, book));
                }
                return copies;
            }
        }
    }

    private void updateCopyStatus(Connection connection, int copyId, CopyStatus status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE book_copies SET status = ? WHERE copy_id = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, copyId);
            statement.executeUpdate();
        }
    }

    private void enrichMember(Connection connection, Member member) throws SQLException {
        member.setActiveLoansCount(countActiveLoansByMember(member.getId()));
        member.setUnpaidFines(hasUnpaidFines(member.getId()));
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role = Role.valueOf(resultSet.getString("role"));
        return switch (role) {
            case ADMIN -> new Admin(
                    resultSet.getInt("user_id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getBoolean("active"),
                    resultSet.getString("employee_number"),
                    toLocalDate(resultSet.getDate("hire_date"))
            );
            case LIBRARIAN -> new Librarian(
                    resultSet.getInt("user_id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getBoolean("active"),
                    resultSet.getString("employee_number"),
                    toLocalDate(resultSet.getDate("hire_date"))
            );
            case MEMBER -> new Member(
                    resultSet.getInt("user_id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getBoolean("active"),
                    resultSet.getString("membership_number"),
                    toLocalDate(resultSet.getDate("registration_date")),
                    resultSet.getInt("max_loans"),
                    resultSet.getString("address")
            );
        };
    }

    private Member mapLoanMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getInt("member_user_id"),
                resultSet.getString("member_first_name"),
                resultSet.getString("member_last_name"),
                resultSet.getString("member_email"),
                resultSet.getString("member_phone"),
                resultSet.getString("member_username"),
                resultSet.getString("member_password_hash"),
                resultSet.getBoolean("member_active"),
                resultSet.getString("membership_number"),
                toLocalDate(resultSet.getDate("registration_date")),
                resultSet.getInt("max_loans"),
                resultSet.getString("address")
        );
    }

    private Librarian mapLoanLibrarian(ResultSet resultSet) throws SQLException {
        return new Librarian(
                resultSet.getInt("staff_user_id"),
                resultSet.getString("staff_first_name"),
                resultSet.getString("staff_last_name"),
                resultSet.getString("staff_email"),
                resultSet.getString("staff_phone"),
                resultSet.getString("staff_username"),
                resultSet.getString("staff_password_hash"),
                resultSet.getBoolean("staff_active"),
                resultSet.getString("employee_number"),
                toLocalDate(resultSet.getDate("hire_date"))
        );
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        Category category = null;
        int categoryId = resultSet.getInt("category_id");
        if (!resultSet.wasNull()) {
            category = new Category(categoryId, resultSet.getString("category_name"), resultSet.getString("category_description"));
        }
        return new Book(
                resultSet.getInt("book_id"),
                resultSet.getString("isbn"),
                resultSet.getString("title"),
                resultSet.getInt("publication_year"),
                resultSet.getString("publisher"),
                category,
                resultSet.getBoolean("active")
        );
    }

    private BookCopy mapCopy(ResultSet resultSet, Book book) throws SQLException {
        return new BookCopy(
                resultSet.getInt("copy_id"),
                resultSet.getString("barcode"),
                CopyStatus.valueOf(resultSet.getString("status") != null ? resultSet.getString("status") : resultSet.getString("copy_status")),
                book,
                toLocalDate(resultSet.getDate("acquisition_date"))
        );
    }

    private Loan mapLoan(ResultSet resultSet) throws SQLException {
        Book book = mapBook(resultSet);
        BookCopy copy = new BookCopy(
                resultSet.getInt("copy_id"),
                resultSet.getString("barcode"),
                CopyStatus.valueOf(resultSet.getString("copy_status")),
                book,
                toLocalDate(resultSet.getDate("acquisition_date"))
        );
        return new Loan(
                resultSet.getInt("loan_id"),
                mapLoanMember(resultSet),
                copy,
                mapLoanLibrarian(resultSet),
                toLocalDate(resultSet.getDate("borrow_date")),
                toLocalDate(resultSet.getDate("due_date")),
                toLocalDate(resultSet.getDate("return_date")),
                LoanStatus.valueOf(resultSet.getString("status"))
        );
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        Member member = (Member) mapUser(resultSet);
        return new Reservation(
                resultSet.getInt("reservation_id"),
                mapBook(resultSet),
                member,
                toLocalDate(resultSet.getDate("reservation_date")),
                toLocalDate(resultSet.getDate("expiry_date")),
                ReservationStatus.valueOf(resultSet.getString("status"))
        );
    }

    private Category mapCategory(ResultSet resultSet) throws SQLException {
        return new Category(resultSet.getInt("category_id"), resultSet.getString("name"), resultSet.getString("description"));
    }

    private Author mapAuthor(ResultSet resultSet) throws SQLException {
        return new Author(resultSet.getInt("author_id"), resultSet.getString("first_name"), resultSet.getString("last_name"));
    }

    private void fillUserStatement(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPasswordHash());
        statement.setString(3, user.getRole().name());
        statement.setString(4, user.getFirstName());
        statement.setString(5, user.getLastName());
        statement.setString(6, user.getEmail());
        statement.setString(7, user.getPhone());
        statement.setBoolean(8, user.isActive());
    }

    private void fillBookStatement(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, blankToNull(book.getIsbn()));
        statement.setString(2, book.getTitle());
        statement.setString(3, book.getPublisher());
        if (book.getPublicationYear() > 0) {
            statement.setInt(4, book.getPublicationYear());
        } else {
            statement.setNull(4, java.sql.Types.INTEGER);
        }
        if (book.getCategory() != null && book.getCategory().getCategoryId() > 0) {
            statement.setInt(5, book.getCategory().getCategoryId());
        } else {
            statement.setNull(5, java.sql.Types.INTEGER);
        }
        statement.setBoolean(6, book.isActive());
    }

    private int readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Aucun identifiant genere.");
        }
    }

    private void setDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
        if (date == null) {
            statement.setNull(index, java.sql.Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
