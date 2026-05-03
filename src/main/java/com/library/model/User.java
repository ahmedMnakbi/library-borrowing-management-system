package com.library.model;

import com.library.enums.Role;

public abstract class User extends Person {
    private String username;
    private String passwordHash;
    private Role role;
    private boolean active;

    protected User() {
    }

    protected User(int id, String firstName, String lastName, String email, String phone,
                   String username, String passwordHash, Role role, boolean active) {
        super(id, firstName, lastName, email, phone);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
