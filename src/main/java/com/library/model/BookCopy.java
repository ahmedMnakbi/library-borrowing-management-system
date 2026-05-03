package com.library.model;

import com.library.enums.CopyStatus;

import java.time.LocalDate;

public class BookCopy {
    private int copyId;
    private String barcode;
    private CopyStatus status;
    private Book book;
    private LocalDate acquisitionDate;

    public BookCopy() {
    }

    public BookCopy(int copyId, String barcode, CopyStatus status, Book book, LocalDate acquisitionDate) {
        this.copyId = copyId;
        this.barcode = barcode;
        this.status = status;
        this.book = book;
        this.acquisitionDate = acquisitionDate;
    }

    public int getCopyId() {
        return copyId;
    }

    public void setCopyId(int copyId) {
        this.copyId = copyId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
}


