package com.library.model;

import com.library.enums.LoanStatus;
import com.library.exception.InvalidReturnException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanTest {
    @Test
    void shouldDetectDelayDays() {
        Loan loan = new Loan();
        loan.setDueDate(LocalDate.of(2026, 4, 10));

        assertEquals(4, loan.calculateDelayDays(LocalDate.of(2026, 4, 14)));
    }

    @Test
    void shouldMarkLoanAsReturned() {
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.ONGOING);

        loan.markAsReturned(LocalDate.of(2026, 4, 12));

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(LocalDate.of(2026, 4, 12), loan.getReturnDate());
    }

    @Test
    void shouldRejectDoubleReturn() {
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.RETURNED);

        assertThrows(InvalidReturnException.class, () -> loan.markAsReturned(LocalDate.now()));
    }

    @Test
    void shouldReportOverdueWhenDueDatePassedAndNotReturned() {
        Loan loan = new Loan();
        loan.setDueDate(LocalDate.now().minusDays(1));
        loan.setStatus(LoanStatus.ONGOING);

        assertTrue(loan.isOverdue());
    }
}
