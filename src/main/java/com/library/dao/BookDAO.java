package com.library.dao;

import com.library.enums.CopyStatus;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Category;
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

public class BookDAO implements GenericDAO<Book, Integer> {
    private final AuthorDAO authorDAO = new AuthorDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public Optional<Book> findById(Integer id) throws SQLException {
        String sql = """
                SELECT b.book_id, b.isbn, b.title, b.publisher, b.publication_year, b.active,
                       c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON c.category_id = b.category_id
                WHERE b.book_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Book book = mapBook(resultSet);
                    book.setAuthors(findAuthorsByBookId(book.getBookId(), connection));
                    book.setCopies(findCopiesByBookId(book.getBookId(), connection));
                    return Optional.of(book);
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Book> findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT book_id FROM books WHERE isbn = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return findById(resultSet.getInt("book_id"));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {
        String sql = """
                SELECT b.book_id, b.isbn, b.title, b.publisher, b.publication_year, b.active,
                       c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON c.category_id = b.category_id
                ORDER BY b.title
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                Book book = mapBook(resultSet);
                book.setAuthors(findAuthorsByBookId(book.getBookId(), connection));
                book.setCopies(findCopiesByBookId(book.getBookId(), connection));
                books.add(book);
            }
            return books;
        }
    }

    public List<Book> search(String keyword) throws SQLException {
        String sql = """
                SELECT DISTINCT b.book_id, b.isbn, b.title, b.publisher, b.publication_year, b.active,
                       c.category_id, c.name AS category_name, c.description AS category_description
                FROM books b
                LEFT JOIN categories c ON c.category_id = b.category_id
                LEFT JOIN book_authors ba ON ba.book_id = b.book_id
                LEFT JOIN authors a ON a.author_id = ba.author_id
                WHERE b.title LIKE ? OR b.isbn LIKE ? OR a.first_name LIKE ? OR a.last_name LIKE ? OR c.name LIKE ?
                ORDER BY b.title
                """;
        String pattern = "%" + keyword + "%";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 5; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (resultSet.next()) {
                    Book book = mapBook(resultSet);
                    book.setAuthors(findAuthorsByBookId(book.getBookId(), connection));
                    book.setCopies(findCopiesByBookId(book.getBookId(), connection));
                    books.add(book);
                }
                return books;
            }
        }
    }

    @Override
    public Book save(Book entity) throws SQLException {
        String sql = """
                INSERT INTO books (isbn, title, publisher, publication_year, category_id, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, entity.getIsbn());
                statement.setString(2, entity.getTitle());
                statement.setString(3, entity.getPublisher());
                statement.setInt(4, entity.getPublicationYear());
                if (entity.getCategory() != null && entity.getCategory().getCategoryId() > 0) {
                    statement.setInt(5, entity.getCategory().getCategoryId());
                } else {
                    statement.setObject(5, null);
                }
                statement.setBoolean(6, entity.isActive());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        entity.setBookId(keys.getInt(1));
                    }
                }
                replaceAuthors(connection, entity);
                connection.commit();
                return entity;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void update(Book entity) throws SQLException {
        String sql = """
                UPDATE books
                SET isbn = ?, title = ?, publisher = ?, publication_year = ?, category_id = ?, active = ?
                WHERE book_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, entity.getIsbn());
                statement.setString(2, entity.getTitle());
                statement.setString(3, entity.getPublisher());
                statement.setInt(4, entity.getPublicationYear());
                if (entity.getCategory() != null && entity.getCategory().getCategoryId() > 0) {
                    statement.setInt(5, entity.getCategory().getCategoryId());
                } else {
                    statement.setObject(5, null);
                }
                statement.setBoolean(6, entity.isActive());
                statement.setInt(7, entity.getBookId());
                statement.executeUpdate();
                replaceAuthors(connection, entity);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE books SET active = FALSE WHERE book_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public int countActiveLoansForBook(int bookId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM loans l
                JOIN book_copies bc ON bc.copy_id = l.copy_id
                WHERE bc.book_id = ? AND l.status IN ('ONGOING', 'OVERDUE')
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
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

    private List<Author> findAuthorsByBookId(int bookId, Connection connection) throws SQLException {
        String sql = """
                SELECT a.author_id, a.first_name, a.last_name
                FROM authors a
                JOIN book_authors ba ON ba.author_id = a.author_id
                WHERE ba.book_id = ?
                ORDER BY a.last_name, a.first_name
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Author> authors = new ArrayList<>();
                while (resultSet.next()) {
                    authors.add(new Author(
                            resultSet.getInt("author_id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name")
                    ));
                }
                return authors;
            }
        }
    }

    private List<BookCopy> findCopiesByBookId(int bookId, Connection connection) throws SQLException {
        String sql = """
                SELECT copy_id, barcode, status, acquisition_date
                FROM book_copies
                WHERE book_id = ?
                ORDER BY copy_id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BookCopy> copies = new ArrayList<>();
                while (resultSet.next()) {
                    copies.add(new BookCopy(
                            resultSet.getInt("copy_id"),
                            resultSet.getString("barcode"),
                            CopyStatus.valueOf(resultSet.getString("status")),
                            null,
                            resultSet.getDate("acquisition_date") != null ? resultSet.getDate("acquisition_date").toLocalDate() : null
                    ));
                }
                return copies;
            }
        }
    }

    private void replaceAuthors(Connection connection, Book entity) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM book_authors WHERE book_id = ?")) {
            deleteStatement.setInt(1, entity.getBookId());
            deleteStatement.executeUpdate();
        }
        String insertSql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            for (Author author : entity.getAuthors()) {
                int authorId = author.getAuthorId();
                if (authorId <= 0) {
                    Author savedAuthor = authorDAO.save(author);
                    authorId = savedAuthor.getAuthorId();
                }
                insertStatement.setInt(1, entity.getBookId());
                insertStatement.setInt(2, authorId);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }
}
