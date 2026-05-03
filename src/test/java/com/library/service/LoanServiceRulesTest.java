package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.FineDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReservationDAO;
import com.library.enums.CopyStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.LoanLimitExceededException;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanServiceRulesTest {
    @Test
    void shouldRejectBorrowWhenCopyIsNotAvailable() {
        LoanService service = new LoanService(
                new EligibleMemberDAO(),
                new UnavailableCopyDAO(),
                new LoanDAO(),
                new FineDAO(),
                new ReservationDAO(),
                new StandardFineCalculator()
        );

        assertThrows(BookUnavailableException.class, () -> service.borrowBook(1, 1, 7, LocalDate.now()));
    }

    @Test
    void shouldRejectBorrowWhenMemberReachedLoanLimit() {
        LoanService service = new LoanService(
                new BlockedMemberDAO(),
                new AvailableCopyDAO(),
                new LoanDAO(),
                new FineDAO(),
                new ReservationDAO(),
                new StandardFineCalculator()
        );

        assertThrows(LoanLimitExceededException.class, () -> service.borrowBook(1, 1, 7, LocalDate.now()));
    }

    private static class EligibleMemberDAO extends MemberDAO {
        @Override
        public Optional<Member> findById(Integer id) {
            Member member = new Member();
            member.setId(id);
            member.setActive(true);
            member.setMaxLoans(3);
            member.setActiveLoansCount(0);
            member.setUnpaidFines(false);
            return Optional.of(member);
        }

        @Override
        public void enrichMember(Member member) {
        }
    }

    private static class BlockedMemberDAO extends MemberDAO {
        @Override
        public Optional<Member> findById(Integer id) {
            Member member = new Member();
            member.setId(id);
            member.setActive(true);
            member.setMaxLoans(3);
            member.setActiveLoansCount(3);
            member.setUnpaidFines(false);
            return Optional.of(member);
        }

        @Override
        public void enrichMember(Member member) {
        }
    }

    private static class AvailableCopyDAO extends BookCopyDAO {
        @Override
        public Optional<BookCopy> findById(Integer id) {
            Book book = new Book();
            book.setBookId(10);
            book.setTitle("Available book");

            BookCopy copy = new BookCopy();
            copy.setCopyId(id);
            copy.setStatus(CopyStatus.AVAILABLE);
            copy.setBook(book);
            return Optional.of(copy);
        }
    }

    private static class UnavailableCopyDAO extends BookCopyDAO {
        @Override
        public Optional<BookCopy> findById(Integer id) {
            Book book = new Book();
            book.setBookId(10);
            book.setTitle("Unavailable book");

            BookCopy copy = new BookCopy();
            copy.setCopyId(id);
            copy.setStatus(CopyStatus.BORROWED);
            copy.setBook(book);
            return Optional.of(copy);
        }
    }
}
