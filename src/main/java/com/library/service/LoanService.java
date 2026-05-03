package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.FineDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReservationDAO;
import com.library.enums.CopyStatus;
import com.library.enums.FineStatus;
import com.library.enums.LoanStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.EntityNotFoundException;
import com.library.exception.InvalidReturnException;
import com.library.exception.LoanLimitExceededException;
import com.library.model.Fine;
import com.library.model.Librarian;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoanService {
    public static final int STANDARD_LOAN_DAYS = 14;
    public static final int MAX_ACTIVE_LOANS = 3;

    private final MemberDAO memberDAO;
    private final BookCopyDAO bookCopyDAO;
    private final LoanDAO loanDAO;
    private final FineDAO fineDAO;
    private final ReservationDAO reservationDAO;
    private final FineCalculator fineCalculator;

    public LoanService() {
        this(new MemberDAO(), new BookCopyDAO(), new LoanDAO(), new FineDAO(), new ReservationDAO(), new StandardFineCalculator());
    }

    public LoanService(MemberDAO memberDAO, BookCopyDAO bookCopyDAO, LoanDAO loanDAO,
                       FineDAO fineDAO, ReservationDAO reservationDAO, FineCalculator fineCalculator) {
        this.memberDAO = memberDAO;
        this.bookCopyDAO = bookCopyDAO;
        this.loanDAO = loanDAO;
        this.fineDAO = fineDAO;
        this.reservationDAO = reservationDAO;
        this.fineCalculator = fineCalculator;
    }

    public Loan borrowBook(int memberId, int copyId, int librarianId) throws SQLException {
        return borrowBook(memberId, copyId, librarianId, LocalDate.now());
    }

    public Loan borrowBook(int memberId, int copyId, int librarianId, LocalDate borrowDate) throws SQLException {
        Member member = memberDAO.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Adherent introuvable."));
        memberDAO.enrichMember(member);

        if (!member.isActive()) {
            throw new LoanLimitExceededException("Cet adherent est inactif.");
        }
        if (member.hasUnpaidFines()) {
            throw new LoanLimitExceededException("Cet adherent a des penalites impayees.");
        }
        if (member.getActiveLoansCount() >= Math.min(member.getMaxLoans(), MAX_ACTIVE_LOANS)) {
            throw new LoanLimitExceededException("La limite d'emprunts actifs est atteinte.");
        }

        var copy = bookCopyDAO.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaire introuvable."));
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BookUnavailableException("Seul un exemplaire disponible peut etre emprunte.");
        }

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBookCopy(copy);
        Librarian librarian = new Librarian();
        librarian.setId(librarianId);
        loan.setLibrarian(librarian);
        loan.setBorrowDate(borrowDate);
        loan.setDueDate(borrowDate.plusDays(STANDARD_LOAN_DAYS));
        loan.setStatus(LoanStatus.ONGOING);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                loanDAO.save(connection, loan);
                bookCopyDAO.updateStatus(connection, copyId, CopyStatus.BORROWED);
                connection.commit();
                return loan;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public BigDecimal returnBook(int loanId) throws SQLException {
        return returnBook(loanId, LocalDate.now());
    }

    public BigDecimal returnBook(int loanId, LocalDate actualReturnDate) throws SQLException {
        Loan loan = loanDAO.findActiveById(loanId)
                .orElseThrow(() -> new InvalidReturnException("Emprunt actif introuvable ou deja retourne."));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new InvalidReturnException("Un retour ne peut etre enregistre qu'une seule fois.");
        }

        BigDecimal fineAmount = fineCalculator.calculateFine(loan, actualReturnDate);
        loan.markAsReturned(actualReturnDate);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                loanDAO.update(connection, loan);
                CopyStatus nextCopyStatus = CopyStatus.AVAILABLE;
                List<Reservation> pendingReservations = reservationDAO.findPendingByBookId(loan.getBookCopy().getBook().getBookId());
                if (!pendingReservations.isEmpty()) {
                    Reservation firstReservation = pendingReservations.get(0);
                    if (firstReservation.getStatus() == ReservationStatus.PENDING) {
                        reservationDAO.fulfill(firstReservation.getReservationId());
                        nextCopyStatus = CopyStatus.RESERVED;
                    }
                }
                bookCopyDAO.updateStatus(connection, loan.getBookCopy().getCopyId(), nextCopyStatus);

                if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Fine fine = new Fine();
                    fine.setLoan(loan);
                    fine.setAmount(fineAmount);
                    fine.setReason("Retard de " + loan.calculateDelayDays(actualReturnDate) + " jour(s).");
                    fine.setStatus(FineStatus.UNPAID);
                    fine.setCreatedAt(LocalDateTime.now());
                    fineDAO.save(connection, fine);
                }
                connection.commit();
                return fineAmount;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Loan> listActiveLoans() throws SQLException {
        return loanDAO.findActiveLoans();
    }

    public List<Loan> listOverdueLoans() throws SQLException {
        return loanDAO.findOverdueLoans();
    }

    public Loan getLoanById(int loanId) throws SQLException {
        return loanDAO.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Emprunt introuvable."));
    }
}
