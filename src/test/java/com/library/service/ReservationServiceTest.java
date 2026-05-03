package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReservationDAO;
import com.library.enums.CopyStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BookUnavailableException;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Member;
import com.library.model.Reservation;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceTest {
    @Test
    void shouldRejectDuplicatePendingReservation() {
        ReservationService service = new ReservationService(
                new FakeReservationDAO(true),
                new FakeBookDAO(),
                new FakeMemberDAO(),
                new FakeUnavailableBookCopyDAO()
        );

        assertThrows(BookUnavailableException.class, () -> service.createReservation(1, 1));
    }

    @Test
    void shouldCreatePendingReservationWithDefaultExpiry() throws SQLException {
        FakeReservationDAO reservationDAO = new FakeReservationDAO(false);
        ReservationService service = new ReservationService(
                reservationDAO,
                new FakeBookDAO(),
                new FakeMemberDAO(),
                new FakeUnavailableBookCopyDAO()
        );

        Reservation reservation = service.createReservation(1, 1);

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertEquals(LocalDate.now().plusDays(3), reservation.getExpiryDate());
    }

    private static class FakeReservationDAO extends ReservationDAO {
        private final boolean duplicate;

        private FakeReservationDAO(boolean duplicate) {
            this.duplicate = duplicate;
        }

        @Override
        public boolean existsPendingForMemberAndBook(int memberId, int bookId) {
            return duplicate;
        }

        @Override
        public Reservation save(Reservation entity) {
            entity.setReservationId(99);
            return entity;
        }

        @Override
        public List<Reservation> findAll() {
            return List.of();
        }
    }

    private static class FakeBookDAO extends BookDAO {
        @Override
        public Optional<Book> findById(Integer id) {
            Book book = new Book();
            book.setBookId(id);
            book.setTitle("Test");
            return Optional.of(book);
        }
    }

    private static class FakeMemberDAO extends MemberDAO {
        @Override
        public Optional<Member> findById(Integer id) {
            Member member = new Member();
            member.setId(id);
            member.setFirstName("Ada");
            member.setLastName("Lovelace");
            return Optional.of(member);
        }
    }

    private static class FakeUnavailableBookCopyDAO extends BookCopyDAO {
        @Override
        public List<BookCopy> findAvailableByBookId(int bookId) {
            return List.of();
        }
    }
}
