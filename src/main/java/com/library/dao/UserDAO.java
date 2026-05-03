package com.library.dao;

import com.library.enums.Role;
import com.library.model.Admin;
import com.library.model.Librarian;
import com.library.model.Member;
import com.library.model.Staff;
import com.library.model.User;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO implements GenericDAO<User, Integer> {
    private static final String USER_BASE_QUERY = """
            SELECT u.user_id, u.username, u.password_hash, u.role, u.first_name, u.last_name, u.email, u.phone, u.active,
                   m.membership_number, m.address, m.registration_date, m.max_loans,
                   s.employee_number, s.hire_date
            FROM users u
            LEFT JOIN members m ON m.member_id = u.user_id
            LEFT JOIN staff s ON s.staff_id = u.user_id
            """;

    @Override
    public Optional<User> findById(Integer id) throws SQLException {
        String sql = USER_BASE_QUERY + " WHERE u.user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = USER_BASE_QUERY + " WHERE u.username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<User> search(String keyword) throws SQLException {
        String sql = USER_BASE_QUERY + """
                 WHERE u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?
                 ORDER BY u.created_at DESC
                """;
        String pattern = "%" + keyword + "%";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = USER_BASE_QUERY + " ORDER BY u.created_at DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        }
    }

    @Override
    public User save(User entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertUser(connection, entity);
                insertRoleSpecificRow(connection, entity);
                connection.commit();
                return entity;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void update(User entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String sql = """
                        UPDATE users
                        SET username = ?, password_hash = ?, role = ?, first_name = ?, last_name = ?, email = ?, phone = ?, active = ?
                        WHERE user_id = ?
                        """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, entity.getUsername());
                    statement.setString(2, entity.getPasswordHash());
                    statement.setString(3, entity.getRole().name());
                    statement.setString(4, entity.getFirstName());
                    statement.setString(5, entity.getLastName());
                    statement.setString(6, entity.getEmail());
                    statement.setString(7, entity.getPhone());
                    statement.setBoolean(8, entity.isActive());
                    statement.setInt(9, entity.getId());
                    statement.executeUpdate();
                }
                upsertRoleSpecificRow(connection, entity);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        updateActiveStatus(id, false);
    }

    public void reactivate(Integer id) throws SQLException {
        updateActiveStatus(id, true);
    }

    private void insertUser(Connection connection, User entity) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, role, first_name, last_name, email, phone, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getPasswordHash());
            statement.setString(3, entity.getRole().name());
            statement.setString(4, entity.getFirstName());
            statement.setString(5, entity.getLastName());
            statement.setString(6, entity.getEmail());
            statement.setString(7, entity.getPhone());
            statement.setBoolean(8, entity.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
        }
    }

    private void insertRoleSpecificRow(Connection connection, User entity) throws SQLException {
        if (entity instanceof Member member) {
            String sql = """
                    INSERT INTO members (member_id, membership_number, address, registration_date, max_loans)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, member.getId());
                statement.setString(2, member.getMembershipNumber());
                statement.setString(3, member.getAddress());
                statement.setDate(4, Date.valueOf(member.getRegistrationDate()));
                statement.setInt(5, member.getMaxLoans());
                statement.executeUpdate();
            }
        } else if (entity instanceof Staff staff) {
            String sql = """
                    INSERT INTO staff (staff_id, employee_number, hire_date)
                    VALUES (?, ?, ?)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, staff.getId());
                statement.setString(2, staff.getEmployeeNumber());
                if (staff.getHireDate() != null) {
                    statement.setDate(3, Date.valueOf(staff.getHireDate()));
                } else {
                    statement.setDate(3, null);
                }
                statement.executeUpdate();
            }
        }
    }

    private void upsertRoleSpecificRow(Connection connection, User entity) throws SQLException {
        if (entity instanceof Member member) {
            String sql = """
                    INSERT INTO members (member_id, membership_number, address, registration_date, max_loans)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE membership_number = VALUES(membership_number),
                                            address = VALUES(address),
                                            registration_date = VALUES(registration_date),
                                            max_loans = VALUES(max_loans)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, member.getId());
                statement.setString(2, member.getMembershipNumber());
                statement.setString(3, member.getAddress());
                statement.setDate(4, Date.valueOf(member.getRegistrationDate()));
                statement.setInt(5, member.getMaxLoans());
                statement.executeUpdate();
            }
        } else if (entity instanceof Staff staff) {
            String sql = """
                    INSERT INTO staff (staff_id, employee_number, hire_date)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE employee_number = VALUES(employee_number),
                                            hire_date = VALUES(hire_date)
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, staff.getId());
                statement.setString(2, staff.getEmployeeNumber());
                if (staff.getHireDate() != null) {
                    statement.setDate(3, Date.valueOf(staff.getHireDate()));
                } else {
                    statement.setDate(3, null);
                }
                statement.executeUpdate();
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role = Role.valueOf(resultSet.getString("role"));
        return switch (role) {
            case MEMBER -> {
                Member member = new Member(
                        resultSet.getInt("user_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        resultSet.getBoolean("active"),
                        resultSet.getString("membership_number"),
                        resultSet.getDate("registration_date") != null ? resultSet.getDate("registration_date").toLocalDate() : null,
                        resultSet.getInt("max_loans"),
                        resultSet.getString("address")
                );
                yield member;
            }
            case LIBRARIAN -> new Librarian(
                    resultSet.getInt("user_id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getBoolean("active"),
                    resultSet.getString("employee_number"),
                    resultSet.getDate("hire_date") != null ? resultSet.getDate("hire_date").toLocalDate() : null
            );
            case ADMIN -> new Admin(
                    resultSet.getInt("user_id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("phone"),
                    resultSet.getString("username"),
                    resultSet.getString("password_hash"),
                    resultSet.getBoolean("active"),
                    resultSet.getString("employee_number"),
                    resultSet.getDate("hire_date") != null ? resultSet.getDate("hire_date").toLocalDate() : null
            );
        };
    }

    private void updateActiveStatus(Integer id, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }
}
