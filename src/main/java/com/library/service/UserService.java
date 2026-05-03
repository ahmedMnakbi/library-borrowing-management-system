package com.library.service;

import com.library.dao.StaffDAO;
import com.library.dao.UserDAO;
import com.library.enums.Role;
import com.library.exception.EntityNotFoundException;
import com.library.model.Admin;
import com.library.model.Librarian;
import com.library.model.Staff;
import com.library.model.User;
import com.library.util.PasswordUtil;
import com.library.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;
    private final StaffDAO staffDAO;

    public UserService() {
        this(new UserDAO(), new StaffDAO());
    }

    public UserService(UserDAO userDAO, StaffDAO staffDAO) {
        this.userDAO = userDAO;
        this.staffDAO = staffDAO;
    }

    public Staff createStaffAccount(Role role, String username, String plainPassword, String firstName, String lastName,
                                    String email, String phone, String employeeNumber, LocalDate hireDate) throws SQLException {
        Validator.requireNotBlank(username, "Le nom d'utilisateur est obligatoire.");
        Validator.requireNotBlank(plainPassword, "Le mot de passe est obligatoire.");
        Validator.requireNotBlank(firstName, "Le prenom est obligatoire.");
        Validator.requireNotBlank(lastName, "Le nom est obligatoire.");
        Validator.requireNotBlank(employeeNumber, "Le numero employe est obligatoire.");
        Validator.requireValidEmail(email, "L'email est invalide.");

        Staff staff = role == Role.ADMIN ? new Admin() : new Librarian();
        staff.setUsername(username);
        staff.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setEmail(email);
        staff.setPhone(phone);
        staff.setActive(true);
        staff.setEmployeeNumber(employeeNumber);
        staff.setHireDate(hireDate != null ? hireDate : LocalDate.now());
        return staffDAO.save(staff);
    }

    public void updateUser(User user) throws SQLException {
        Validator.requireNotBlank(user.getUsername(), "Le nom d'utilisateur est obligatoire.");
        Validator.requireNotBlank(user.getFirstName(), "Le prenom est obligatoire.");
        Validator.requireNotBlank(user.getLastName(), "Le nom est obligatoire.");
        Validator.requireValidEmail(user.getEmail(), "L'email est invalide.");
        userDAO.update(user);
    }

    public void changePassword(int userId, String newPassword) throws SQLException {
        User user = getUserById(userId);
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.update(user);
    }

    public void deactivateUser(int userId) throws SQLException {
        userDAO.delete(userId);
    }

    public void reactivateUser(int userId) throws SQLException {
        userDAO.reactivate(userId);
    }

    public User getUserById(int userId) throws SQLException {
        return userDAO.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
    }

    public List<User> listUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        return userDAO.search(keyword);
    }
}
