package com.library.model;

import com.library.enums.Role;

import java.time.LocalDate;

public class Admin extends Staff {
    public Admin() {
        setRole(Role.ADMIN);
    }

    public Admin(int id, String firstName, String lastName, String email, String phone,
                 String username, String passwordHash, boolean active,
                 String employeeNumber, LocalDate hireDate) {
        super(id, firstName, lastName, email, phone, username, passwordHash, Role.ADMIN, active, employeeNumber, hireDate);
    }
}
