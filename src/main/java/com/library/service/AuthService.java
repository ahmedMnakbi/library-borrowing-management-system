package com.library.service;

import com.library.dao.UserDAO;
import com.library.exception.AuthenticationException;
import com.library.model.User;
import com.library.util.PasswordUtil;
import com.library.util.Validator;

import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String plainPassword) throws SQLException {
        Validator.requireNotBlank(username, "Le nom d'utilisateur est obligatoire.");
        Validator.requireNotBlank(plainPassword, "Le mot de passe est obligatoire.");

        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Identifiants invalides."));

        if (!user.isActive()) {
            throw new AuthenticationException("Ce compte est desactive.");
        }

        if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Identifiants invalides.");
        }
        return user;
    }
}
