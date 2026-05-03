package com.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberTest {
    @Test
    void shouldAllowBorrowWhenActiveBelowLimitAndNoFine() {
        Member member = new Member();
        member.setActive(true);
        member.setMaxLoans(3);
        member.setActiveLoansCount(1);
        member.setUnpaidFines(false);

        assertTrue(member.canBorrow());
    }

    @Test
    void shouldBlockBorrowWhenHasUnpaidFine() {
        Member member = new Member();
        member.setActive(true);
        member.setMaxLoans(3);
        member.setActiveLoansCount(1);
        member.setUnpaidFines(true);

        assertFalse(member.canBorrow());
    }

    @Test
    void shouldBlockBorrowWhenLimitReached() {
        Member member = new Member();
        member.setActive(true);
        member.setMaxLoans(3);
        member.setActiveLoansCount(3);
        member.setUnpaidFines(false);

        assertFalse(member.canBorrow());
    }
}
