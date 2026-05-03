package com.library.model;

import com.library.enums.LoanStatus;
import com.library.exception.InvalidReturnException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private int loanId;
    private Member member;
    private BookCopy bookCopy;
    private Librarian librarian;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public Loan() {
    }

    public Loan(int loanId, Member member, BookCopy bookCopy, Librarian librarian,
                LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, LoanStatus status) {
        this.loanId = loanId;
        this.member = member;
        this.bookCopy = bookCopy;
        this.librarian = librarian;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public boolean isOverdue() {
        LocalDate comparisonDate = returnDate != null ? returnDate : LocalDate.now();
        return dueDate != null && comparisonDate.isAfter(dueDate) && status != LoanStatus.RETURNED;
    }

    public void markAsReturned(LocalDate actualReturnDate) {
        if (status == LoanStatus.RETURNED) {
            throw new InvalidReturnException("Cet emprunt a deja ete retourne.");
        }
        this.returnDate = actualReturnDate;
        this.status = LoanStatus.RETURNED;
    }

    public long calculateDelayDays(LocalDate actualReturnDate) {
        if (actualReturnDate == null || dueDate == null || !actualReturnDate.isAfter(dueDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, actualReturnDate);
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public Librarian getLibrarian() {
        return librarian;
    }

    public void setLibrarian(Librarian librarian) {
        this.librarian = librarian;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }
}
