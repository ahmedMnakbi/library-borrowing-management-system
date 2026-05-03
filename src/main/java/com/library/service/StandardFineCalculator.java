package com.library.service;

import com.library.model.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StandardFineCalculator implements FineCalculator {
    private final BigDecimal dailyRate;

    public StandardFineCalculator() {
        this(new BigDecimal("2.00"));
    }

    public StandardFineCalculator(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    @Override
    public BigDecimal calculateFine(Loan loan, LocalDate actualReturnDate) {
        long delayDays = loan.calculateDelayDays(actualReturnDate);
        return delayDays <= 0 ? BigDecimal.ZERO : dailyRate.multiply(BigDecimal.valueOf(delayDays));
    }
}
