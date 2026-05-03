package com.library.dao;

import com.library.enums.CopyStatus;
import com.library.enums.LoanStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Librarian;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO implements GenericDAO<Loan, Integer> {
    private static final String LOAN_QUERY = """
            SELECT l.loan_id, l.borrow_date, l.due_date, l.return_date, l.status,
                   bc.copy_id, bc.barcode, bc.status AS copy_status,
                   b.book_id, b.title, b.isbn,
                   m.member_id, m.membership_number, mu.first_name AS member_first_name, mu.last_name AS member_last_name,
                   mu.email AS member_email, mu.phone AS member_phone, mu.username AS member_username,
                   mu.password_hash AS member_password_hash, mu.active AS member_active, m.address, m.registration_date, m.max_loans,
                   s.staff_id, s.employee_number, su.first_name AS librarian_first_name, su.last_name AS librarian_last_name,
                   su.email AS librarian_email, su.phone AS librarian_phone, su.username AS librarian_username,
                   su.password_hash AS librarian_password_hash, su.active AS librarian_active, s.hire_date
            FROM loans l
            JOIN book_copies bc ON bc.copy_id = l.copy_id
            JOIN books b ON b.book_id = bc.book_id
            JOIN members m ON m.member_id = l.member_id
            JOIN users mu ON mu.user_id = m.member_id
            JOIN staff s ON s.staff_id = l.librarian_id
            JOIN users su ON su.user_id = s.staff_id
            """;

    @Override
    public Optional<Loan> findById(Integer id) throws SQLException {
        String sql = LOAN_QUERY + " WHERE l.loan_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLoan(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Loan> findActiveById(int id) throws SQLException {
        String sql = LOAN_QUERY + " WHERE l.loan_id = ? AND l.status IN ('ONGOING', 'OVERDUE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLoan(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Loan> findAll() throws SQLException {
        String sql = LOAN_QUERY + " ORDER BY l.borrow_date DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Loan> loans = new ArrayList<>();
            while (resultSet.next()) {
                loans.add(mapLoan(resultSet));
            }
            return loans;
        }
    }

    public List<Loan> findActiveLoans() throws SQLException {
        String sql = LOAN_QUERY + " WHERE l.status IN ('ONGOING', 'OVERDUE') ORDER BY l.due_date";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Loan> loans = new ArrayList<>();
            while (resultSet.next()) {
                loans.add(mapLoan(resultSet));
            }
            return loans;
        }
    }

    public List<Loan> findOverdueLoans() throws SQLException {
        String sql = LOAN_QUERY + " WHERE l.status IN ('ONGOING', 'OVERDUE') AND l.due_date < CURRENT_DATE ORDER BY l.due_date";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Loan> loans = new ArrayList<>();
            while (resultSet.next()) {
                loans.add(mapLoan(resultSet));
            }
            return loans;
        }
    }

    public List<Loan> findHistoryByMemberId(int memberId) throws SQLException {
        String sql = LOAN_QUERY + " WHERE l.member_id = ? ORDER BY l.borrow_date DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (resultSet.next()) {
                    loans.add(mapLoan(resultSet));
                }
                return loans;
            }
        }
    }

    public int countActiveLoansByMember(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status IN ('ONGOING', 'OVERDUE')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public Loan save(Loan entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return save(connection, entity);
        }
    }

    public Loan save(Connection connection, Loan entity) throws SQLException {
        String sql = """
                INSERT INTO loans (copy_id, member_id, librarian_id, borrow_date, due_date, return_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, entity.getBookCopy().getCopyId());
            statement.setInt(2, entity.getMember().getId());
            statement.setInt(3, entity.getLibrarian().getId());
            statement.setDate(4, Date.valueOf(entity.getBorrowDate()));
            statement.setDate(5, Date.valueOf(entity.getDueDate()));
            if (entity.getReturnDate() != null) {
                statement.setDate(6, Date.valueOf(entity.getReturnDate()));
            } else {
                statement.setDate(6, null);
            }
            statement.setString(7, entity.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setLoanId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(Loan entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            update(connection, entity);
        }
    }

    public void update(Connection connection, Loan entity) throws SQLException {
        String sql = """
                UPDATE loans
                SET copy_id = ?, member_id = ?, librarian_id = ?, borrow_date = ?, due_date = ?, return_date = ?, status = ?
                WHERE loan_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entity.getBookCopy().getCopyId());
            statement.setInt(2, entity.getMember().getId());
            statement.setInt(3, entity.getLibrarian().getId());
            statement.setDate(4, Date.valueOf(entity.getBorrowDate()));
            statement.setDate(5, Date.valueOf(entity.getDueDate()));
            if (entity.getReturnDate() != null) {
                statement.setDate(6, Date.valueOf(entity.getReturnDate()));
            } else {
                statement.setDate(6, null);
            }
            statement.setString(7, entity.getStatus().name());
            statement.setInt(8, entity.getLoanId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Loan mapLoan(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setBookId(resultSet.getInt("book_id"));
        book.setTitle(resultSet.getString("title"));
        book.setIsbn(resultSet.getString("isbn"));

        BookCopy copy = new BookCopy();
        copy.setCopyId(resultSet.getInt("copy_id"));
        copy.setBarcode(resultSet.getString("barcode"));
        copy.setStatus(CopyStatus.valueOf(resultSet.getString("copy_status")));
        copy.setBook(book);

        Member member = new Member(
                resultSet.getInt("member_id"),
                resultSet.getString("member_first_name"),
                resultSet.getString("member_last_name"),
                resultSet.getString("member_email"),
                resultSet.getString("member_phone"),
                resultSet.getString("member_username"),
                resultSet.getString("member_password_hash"),
                resultSet.getBoolean("member_active"),
                resultSet.getString("membership_number"),
                resultSet.getDate("registration_date") != null ? resultSet.getDate("registration_date").toLocalDate() : null,
                resultSet.getInt("max_loans"),
                resultSet.getString("address")
        );

        Librarian librarian = new Librarian(
                resultSet.getInt("staff_id"),
                resultSet.getString("librarian_first_name"),
                resultSet.getString("librarian_last_name"),
                resultSet.getString("librarian_email"),
                resultSet.getString("librarian_phone"),
                resultSet.getString("librarian_username"),
                resultSet.getString("librarian_password_hash"),
                resultSet.getBoolean("librarian_active"),
                resultSet.getString("employee_number"),
                resultSet.getDate("hire_date") != null ? resultSet.getDate("hire_date").toLocalDate() : null
        );

        return new Loan(
                resultSet.getInt("loan_id"),
                member,
                copy,
                librarian,
                resultSet.getDate("borrow_date").toLocalDate(),
                resultSet.getDate("due_date").toLocalDate(),
                resultSet.getDate("return_date") != null ? resultSet.getDate("return_date").toLocalDate() : null,
                LoanStatus.valueOf(resultSet.getString("status"))
        );
    }
}
