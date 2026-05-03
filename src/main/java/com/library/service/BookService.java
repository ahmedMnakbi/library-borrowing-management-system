package com.library.service;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.dao.BookCopyDAO;
import com.library.dao.CategoryDAO;
import com.library.exception.BookUnavailableException;
import com.library.exception.EntityNotFoundException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Category;
import com.library.util.Validator;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDAO bookDAO;
    private final AuthorDAO authorDAO;
    private final CategoryDAO categoryDAO;
    private final BookCopyDAO bookCopyDAO;

    public BookService() {
        this(new BookDAO(), new AuthorDAO(), new CategoryDAO(), new BookCopyDAO());
    }

    public BookService(BookDAO bookDAO, AuthorDAO authorDAO, CategoryDAO categoryDAO, BookCopyDAO bookCopyDAO) {
        this.bookDAO = bookDAO;
        this.authorDAO = authorDAO;
        this.categoryDAO = categoryDAO;
        this.bookCopyDAO = bookCopyDAO;
    }

    public Book createBook(Book book) throws SQLException {
        Validator.requireNotBlank(book.getTitle(), "Le titre est obligatoire.");
        return bookDAO.save(book);
    }

    public void updateBook(Book book) throws SQLException {
        Validator.requireNotBlank(book.getTitle(), "Le titre est obligatoire.");
        bookDAO.update(book);
    }

    public void deactivateBook(int bookId) throws SQLException {
        if (bookDAO.countActiveLoansForBook(bookId) > 0) {
            throw new BookUnavailableException("Ce livre possede des emprunts actifs et ne peut pas etre desactive.");
        }
        bookDAO.delete(bookId);
    }

    public Book getBookById(int bookId) throws SQLException {
        return bookDAO.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Livre introuvable."));
    }

    public List<Book> listBooks() throws SQLException {
        return bookDAO.findAll();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDAO.search(keyword);
    }

    public Category createCategory(String name, String description) throws SQLException {
        Validator.requireNotBlank(name, "Le nom de categorie est obligatoire.");
        return categoryDAO.save(new Category(0, name, description));
    }

    public Author createAuthor(String firstName, String lastName) throws SQLException {
        Validator.requireNotBlank(firstName, "Le prenom de l'auteur est obligatoire.");
        Validator.requireNotBlank(lastName, "Le nom de l'auteur est obligatoire.");
        return authorDAO.save(new Author(0, firstName, lastName));
    }

    public List<Category> listCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public List<Author> listAuthors() throws SQLException {
        return authorDAO.findAll();
    }
}
