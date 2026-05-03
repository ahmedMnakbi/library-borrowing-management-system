package com.library.service;

import com.library.model.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FineCalculator {
    BigDecimal calculateFine(Loan loan, LocalDate actualReturnDate);
}
