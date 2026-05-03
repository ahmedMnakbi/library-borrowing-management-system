package com.library.model;

import com.library.enums.ReservationStatus;

import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private Book book;
    private Member member;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(int reservationId, Book book, Member member, LocalDate reservationDate,
                       LocalDate expiryDate, ReservationStatus status) {
        this.reservationId = reservationId;
        this.book = book;
        this.member = member;
        this.reservationDate = reservationDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
