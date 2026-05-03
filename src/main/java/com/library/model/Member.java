package com.library.model;

import com.library.enums.Role;

import java.time.LocalDate;

public class Member extends User {
    private String membershipNumber;
    private LocalDate registrationDate;
    private int maxLoans;
    private String address;
    private int activeLoansCount;
    private boolean unpaidFines;

    public Member() {
        setRole(Role.MEMBER);
    }

    public Member(int id, String firstName, String lastName, String email, String phone,
                  String username, String passwordHash, boolean active,
                  String membershipNumber, LocalDate registrationDate, int maxLoans, String address) {
        super(id, firstName, lastName, email, phone, username, passwordHash, Role.MEMBER, active);
        this.membershipNumber = membershipNumber;
        this.registrationDate = registrationDate;
        this.maxLoans = maxLoans;
        this.address = address;
    }

    public boolean canBorrow() {
        return isActive() && activeLoansCount < maxLoans && !unpaidFines;
    }

    public int getActiveLoansCount() {
        return activeLoansCount;
    }

    public void setActiveLoansCount(int activeLoansCount) {
        this.activeLoansCount = activeLoansCount;
    }

    public boolean hasUnpaidFines() {
        return unpaidFines;
    }

    public void setUnpaidFines(boolean unpaidFines) {
        this.unpaidFines = unpaidFines;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getMaxLoans() {
        return maxLoans;
    }

    public void setMaxLoans(int maxLoans) {
        this.maxLoans = maxLoans;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
