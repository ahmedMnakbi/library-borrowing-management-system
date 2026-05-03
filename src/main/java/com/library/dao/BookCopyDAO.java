package com.library.dao;

import com.library.enums.CopyStatus;
import com.library.model.Book;
import com.library.model.BookCopy;
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

public class BookCopyDAO implements GenericDAO<BookCopy, Integer> {
    @Override
    public Optional<BookCopy> findById(Integer id) throws SQLException {
        String sql = """
                SELECT bc.copy_id, bc.barcode, bc.status, bc.acquisition_date,
                       b.book_id, b.title, b.isbn
                FROM book_copies bc
                JOIN books b ON b.book_id = bc.book_id
                WHERE bc.copy_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCopy(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<BookCopy> findByBarcode(String barcode) throws SQLException {
        String sql = """
                SELECT bc.copy_id, bc.barcode, bc.status, bc.acquisition_date,
                       b.book_id, b.title, b.isbn
                FROM book_copies bc
                JOIN books b ON b.book_id = bc.book_id
                WHERE bc.barcode = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, barcode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCopy(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<BookCopy> findAll() throws SQLException {
        String sql = """
                SELECT bc.copy_id, bc.barcode, bc.status, bc.acquisition_date,
                       b.book_id, b.title, b.isbn
                FROM book_copies bc
                JOIN books b ON b.book_id = bc.book_id
                ORDER BY bc.copy_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<BookCopy> copies = new ArrayList<>();
            while (resultSet.next()) {
                copies.add(mapCopy(resultSet));
            }
            return copies;
        }
    }

    public List<BookCopy> findAvailableByBookId(int bookId) throws SQLException {
        String sql = """
                SELECT bc.copy_id, bc.barcode, bc.status, bc.acquisition_date,
                       b.book_id, b.title, b.isbn
                FROM book_copies bc
                JOIN books b ON b.book_id = bc.book_id
                WHERE bc.book_id = ? AND bc.status = 'AVAILABLE'
                ORDER BY bc.copy_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BookCopy> copies = new ArrayList<>();
                while (resultSet.next()) {
                    copies.add(mapCopy(resultSet));
                }
                return copies;
            }
        }
    }

    @Override
    public BookCopy save(BookCopy entity) throws SQLException {
        String sql = "INSERT INTO book_copies (book_id, barcode, status, acquisition_date) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, entity.getBook().getBookId());
            statement.setString(2, entity.getBarcode());
            statement.setString(3, entity.getStatus().name());
            if (entity.getAcquisitionDate() != null) {
                statement.setDate(4, Date.valueOf(entity.getAcquisitionDate()));
            } else {
                statement.setDate(4, null);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setCopyId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(BookCopy entity) throws SQLException {
        String sql = "UPDATE book_copies SET book_id = ?, barcode = ?, status = ?, acquisition_date = ? WHERE copy_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entity.getBook().getBookId());
            statement.setString(2, entity.getBarcode());
            statement.setString(3, entity.getStatus().name());
            if (entity.getAcquisitionDate() != null) {
                statement.setDate(4, Date.valueOf(entity.getAcquisitionDate()));
            } else {
                statement.setDate(4, null);
            }
            statement.setInt(5, entity.getCopyId());
            statement.executeUpdate();
        }
    }

    public void updateStatus(int copyId, CopyStatus status) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            updateStatus(connection, copyId, status);
        }
    }

    public void updateStatus(Connection connection, int copyId, CopyStatus status) throws SQLException {
        String sql = "UPDATE book_copies SET status = ? WHERE copy_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, copyId);
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM book_copies WHERE copy_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private BookCopy mapCopy(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setBookId(resultSet.getInt("book_id"));
        book.setTitle(resultSet.getString("title"));
        book.setIsbn(resultSet.getString("isbn"));
        return new BookCopy(
                resultSet.getInt("copy_id"),
                resultSet.getString("barcode"),
                CopyStatus.valueOf(resultSet.getString("status")),
                book,
                resultSet.getDate("acquisition_date") != null ? resultSet.getDate("acquisition_date").toLocalDate() : null
        );
    }
}
