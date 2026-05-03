package com.library.dao;

import com.library.enums.FineStatus;
import com.library.model.Fine;
import com.library.model.Loan;
import com.library.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FineDAO implements GenericDAO<Fine, Integer> {
    @Override
    public Optional<Fine> findById(Integer id) throws SQLException {
        String sql = "SELECT fine_id, loan_id, amount, reason, status, created_at, paid_at FROM fines WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapFine(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Fine> findByLoanId(int loanId) throws SQLException {
        String sql = "SELECT fine_id, loan_id, amount, reason, status, created_at, paid_at FROM fines WHERE loan_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapFine(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Fine> findAll() throws SQLException {
        String sql = "SELECT fine_id, loan_id, amount, reason, status, created_at, paid_at FROM fines ORDER BY created_at DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Fine> fines = new ArrayList<>();
            while (resultSet.next()) {
                fines.add(mapFine(resultSet));
            }
            return fines;
        }
    }

    public List<Fine> findByMemberId(int memberId) throws SQLException {
        String sql = """
                SELECT f.fine_id, f.loan_id, f.amount, f.reason, f.status, f.created_at, f.paid_at
                FROM fines f
                JOIN loans l ON l.loan_id = f.loan_id
                WHERE l.member_id = ?
                ORDER BY f.created_at DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Fine> fines = new ArrayList<>();
                while (resultSet.next()) {
                    fines.add(mapFine(resultSet));
                }
                return fines;
            }
        }
    }

    public List<Fine> findUnpaidFines() throws SQLException {
        String sql = "SELECT fine_id, loan_id, amount, reason, status, created_at, paid_at FROM fines WHERE status = 'UNPAID' ORDER BY created_at DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Fine> fines = new ArrayList<>();
            while (resultSet.next()) {
                fines.add(mapFine(resultSet));
            }
            return fines;
        }
    }

    public boolean hasUnpaidFines(int memberId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM fines f
                JOIN loans l ON l.loan_id = f.loan_id
                WHERE l.member_id = ? AND f.status = 'UNPAID'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public Fine save(Fine entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return save(connection, entity);
        }
    }

    public Fine save(Connection connection, Fine entity) throws SQLException {
        String sql = "INSERT INTO fines (loan_id, amount, reason, status, created_at, paid_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, entity.getLoan().getLoanId());
            statement.setBigDecimal(2, entity.getAmount());
            statement.setString(3, entity.getReason());
            statement.setString(4, entity.getStatus().name());
            statement.setTimestamp(5, entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : null);
            statement.setTimestamp(6, entity.getPaidAt() != null ? Timestamp.valueOf(entity.getPaidAt()) : null);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setFineId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(Fine entity) throws SQLException {
        String sql = "UPDATE fines SET amount = ?, reason = ?, status = ?, paid_at = ? WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, entity.getAmount());
            statement.setString(2, entity.getReason());
            statement.setString(3, entity.getStatus().name());
            statement.setTimestamp(4, entity.getPaidAt() != null ? Timestamp.valueOf(entity.getPaidAt()) : null);
            statement.setInt(5, entity.getFineId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM fines WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void markPaid(int fineId) throws SQLException {
        String sql = "UPDATE fines SET status = 'PAID', paid_at = CURRENT_TIMESTAMP WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fineId);
            statement.executeUpdate();
        }
    }

    public void cancel(int fineId) throws SQLException {
        String sql = "UPDATE fines SET status = 'CANCELLED' WHERE fine_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fineId);
            statement.executeUpdate();
        }
    }

    private Fine mapFine(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(resultSet.getInt("loan_id"));
        return new Fine(
                resultSet.getInt("fine_id"),
                loan,
                resultSet.getBigDecimal("amount"),
                resultSet.getString("reason"),
                FineStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime() : null,
                resultSet.getTimestamp("paid_at") != null ? resultSet.getTimestamp("paid_at").toLocalDateTime() : null
        );
    }
}
