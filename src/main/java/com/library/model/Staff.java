package com.library.model;

import com.library.enums.Role;

import java.time.LocalDate;

public class Staff extends User {
    private String employeeNumber;
    private LocalDate hireDate;

    public Staff() {
    }

    public Staff(int id, String firstName, String lastName, String email, String phone,
                 String username, String passwordHash, Role role, boolean active,
                 String employeeNumber, LocalDate hireDate) {
        super(id, firstName, lastName, email, phone, username, passwordHash, role, active);
        this.employeeNumber = employeeNumber;
        this.hireDate = hireDate;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
