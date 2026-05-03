package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReservationDAO;
import com.library.enums.ReservationStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.EntityNotFoundException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    public static final int DEFAULT_EXPIRY_DAYS = 3;

    private final ReservationDAO reservationDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final BookCopyDAO bookCopyDAO;

    public ReservationService() {
        this(new ReservationDAO(), new BookDAO(), new MemberDAO(), new BookCopyDAO());
    }

    public ReservationService(ReservationDAO reservationDAO, BookDAO bookDAO, MemberDAO memberDAO, BookCopyDAO bookCopyDAO) {
        this.reservationDAO = reservationDAO;
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
        this.bookCopyDAO = bookCopyDAO;
    }

    public Reservation createReservation(int bookId, int memberId) throws SQLException {
        expireOutdatedReservations();
        Book book = bookDAO.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Livre introuvable."));
        Member member = memberDAO.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Adherent introuvable."));

        if (!bookCopyDAO.findAvailableByBookId(bookId).isEmpty()) {
            throw new BookUnavailableException("Une reservation n'est autorisee que si aucun exemplaire n'est disponible.");
        }
        if (reservationDAO.existsPendingForMemberAndBook(memberId, bookId)) {
            throw new BookUnavailableException("Cet adherent a deja une reservation en attente pour ce livre.");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setMember(member);
        reservation.setReservationDate(LocalDate.now());
        reservation.setExpiryDate(LocalDate.now().plusDays(DEFAULT_EXPIRY_DAYS));
        reservation.setStatus(ReservationStatus.PENDING);
        return reservationDAO.save(reservation);
    }

    public void cancelReservation(int reservationId) throws SQLException {
        reservationDAO.cancel(reservationId);
    }

    public List<Reservation> listReservations() throws SQLException {
        expireOutdatedReservations();
        return reservationDAO.findAll();
    }

    public List<Reservation> listMemberReservations(int memberId) throws SQLException {
        expireOutdatedReservations();
        return reservationDAO.findByMemberId(memberId);
    }

    public void expireOutdatedReservations() throws SQLException {
        for (Reservation reservation : reservationDAO.findAll()) {
            if (reservation.getStatus() == ReservationStatus.PENDING
                    && reservation.getExpiryDate() != null
                    && reservation.getExpiryDate().isBefore(LocalDate.now())) {
                reservationDAO.expire(reservation.getReservationId());
            }
        }
    }
}
