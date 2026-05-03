package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.LoanDAO;
import com.library.enums.CopyStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.EntityNotFoundException;
import com.library.model.BookCopy;

import java.sql.SQLException;
import java.util.List;

public class BookCopyService {
    private final BookCopyDAO bookCopyDAO;
    private final LoanDAO loanDAO;

    public BookCopyService() {
        this(new BookCopyDAO(), new LoanDAO());
    }

    public BookCopyService(BookCopyDAO bookCopyDAO, LoanDAO loanDAO) {
        this.bookCopyDAO = bookCopyDAO;
        this.loanDAO = loanDAO;
    }

    public BookCopy createCopy(BookCopy copy) throws SQLException {
        return bookCopyDAO.save(copy);
    }

    public void updateCopy(BookCopy copy) throws SQLException {
        bookCopyDAO.update(copy);
    }

    public void updateStatus(int copyId, CopyStatus status) throws SQLException {
        bookCopyDAO.updateStatus(copyId, status);
    }

    public BookCopy getCopyById(int copyId) throws SQLException {
        return bookCopyDAO.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaire introuvable."));
    }

    public List<BookCopy> listCopies() throws SQLException {
        return bookCopyDAO.findAll();
    }

    public List<BookCopy> listAvailableCopiesByBook(int bookId) throws SQLException {
        return bookCopyDAO.findAvailableByBookId(bookId);
    }

    public void deleteCopy(int copyId) throws SQLException {
        BookCopy copy = getCopyById(copyId);
        if (copy.getStatus() == CopyStatus.BORROWED) {
            throw new BookUnavailableException("Un exemplaire emprunte ne peut pas etre supprime.");
        }
        bookCopyDAO.delete(copyId);
    }
}
