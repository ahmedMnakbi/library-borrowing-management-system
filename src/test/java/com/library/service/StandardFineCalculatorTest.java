package com.library.service;

import com.library.enums.LoanStatus;
import com.library.model.Loan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardFineCalculatorTest {
    private final StandardFineCalculator fineCalculator = new StandardFineCalculator();

    @Test
    void shouldReturnZeroWhenThereIsNoDelay() {
        Loan loan = new Loan();
        loan.setDueDate(LocalDate.of(2026, 4, 10));
        loan.setStatus(LoanStatus.ONGOING);

        BigDecimal fine = fineCalculator.calculateFine(loan, LocalDate.of(2026, 4, 10));

        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void shouldCalculateTwoCurrencyUnitsPerDelayDay() {
        Loan loan = new Loan();
        loan.setDueDate(LocalDate.of(2026, 4, 10));
        loan.setStatus(LoanStatus.ONGOING);

        BigDecimal fine = fineCalculator.calculateFine(loan, LocalDate.of(2026, 4, 13));

        assertEquals(new BigDecimal("6.00"), fine);
    }
}
