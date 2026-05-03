package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private int publicationYear;
    private String publisher;
    private Category category;
    private List<Author> authors = new ArrayList<>();
    private boolean active = true;
    private List<BookCopy> copies = new ArrayList<>();

    public Book() {
    }

    public Book(int bookId, String isbn, String title, int publicationYear, String publisher, Category category, boolean active) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.publicationYear = publicationYear;
        this.publisher = publisher;
        this.category = category;
        this.active = active;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    public long getAvailableCopiesCount() {
        return copies.stream().filter(copy -> copy.getStatus() != null && copy.getStatus().name().equals("AVAILABLE")).count();
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", active=" + active +
                '}';
    }
}
