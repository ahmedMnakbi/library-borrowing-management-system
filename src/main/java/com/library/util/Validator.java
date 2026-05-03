package com.library.util;

import com.library.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validator() {
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new ValidationException(message);
        }
    }

    public static void requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(message);
        }
    }

    public static void requireValidEmail(String email, String message) {
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(message);
        }
    }

    public static void requireDatePresent(LocalDate value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }
}
