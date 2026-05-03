package com.library.model;

import com.library.enums.Role;

import java.time.LocalDate;

public class Librarian extends Staff {
    public Librarian() {
        setRole(Role.LIBRARIAN);
    }

    public Librarian(int id, String firstName, String lastName, String email, String phone,
                     String username, String passwordHash, boolean active,
                     String employeeNumber, LocalDate hireDate) {
        super(id, firstName, lastName, email, phone, username, passwordHash, Role.LIBRARIAN, active, employeeNumber, hireDate);
    }
}
