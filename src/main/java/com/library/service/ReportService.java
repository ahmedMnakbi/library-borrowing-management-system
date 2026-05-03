package com.library.service;

import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    public List<String> activeLoansReport() throws SQLException {
        String sql = """
                SELECT l.loan_id, b.title, mu.first_name, mu.last_name, l.due_date
                FROM loans l
                JOIN book_copies bc ON bc.copy_id = l.copy_id
                JOIN books b ON b.book_id = bc.book_id
                JOIN members m ON m.member_id = l.member_id
                JOIN users mu ON mu.user_id = m.member_id
                WHERE l.status IN ('ONGOING', 'OVERDUE')
                ORDER BY l.due_date
                """;
        return executeSingleLineReport(sql,
                rs -> "#" + rs.getInt("loan_id") + " | " + rs.getString("title") + " | " +
                        rs.getString("first_name") + " " + rs.getString("last_name") + " | echeance " + rs.getDate("due_date"));
    }

    public List<String> overdueLoansReport() throws SQLException {
        String sql = """
                SELECT l.loan_id, b.title, mu.first_name, mu.last_name, l.due_date
                FROM loans l
                JOIN book_copies bc ON bc.copy_id = l.copy_id
                JOIN books b ON b.book_id = bc.book_id
                JOIN members m ON m.member_id = l.member_id
                JOIN users mu ON mu.user_id = m.member_id
                WHERE l.status IN ('ONGOING', 'OVERDUE') AND l.due_date < CURRENT_DATE
                ORDER BY l.due_date
                """;
        return executeSingleLineReport(sql,
                rs -> "#" + rs.getInt("loan_id") + " | " + rs.getString("title") + " | " +
                        rs.getString("first_name") + " " + rs.getString("last_name") + " | retard depuis " + rs.getDate("due_date"));
    }

    public List<String> unpaidFinesReport() throws SQLException {
        String sql = """
                SELECT f.fine_id, f.amount, b.title, u.first_name, u.last_name
                FROM fines f
                JOIN loans l ON l.loan_id = f.loan_id
                JOIN book_copies bc ON bc.copy_id = l.copy_id
                JOIN books b ON b.book_id = bc.book_id
                JOIN members m ON m.member_id = l.member_id
                JOIN users u ON u.user_id = m.member_id
                WHERE f.status = 'UNPAID'
                ORDER BY f.created_at DESC
                """;
        return executeSingleLineReport(sql,
                rs -> "#" + rs.getInt("fine_id") + " | " + rs.getBigDecimal("amount") + " | " +
                        rs.getString("title") + " | " + rs.getString("first_name") + " " + rs.getString("last_name"));
    }

    public List<String> mostBorrowedBooksReport() throws SQLException {
        String sql = """
                SELECT b.title, COUNT(*) AS total
                FROM loans l
                JOIN book_copies bc ON bc.copy_id = l.copy_id
                JOIN books b ON b.book_id = bc.book_id
                GROUP BY b.book_id, b.title
                ORDER BY total DESC, b.title
                LIMIT 10
                """;
        return executeSingleLineReport(sql, rs -> rs.getString("title") + " | emprunts: " + rs.getInt("total"));
    }

    public List<String> mostActiveMembersReport() throws SQLException {
        String sql = """
                SELECT u.first_name, u.last_name, COUNT(*) AS total
                FROM loans l
                JOIN members m ON m.member_id = l.member_id
                JOIN users u ON u.user_id = m.member_id
                GROUP BY m.member_id, u.first_name, u.last_name
                ORDER BY total DESC, u.last_name, u.first_name
                LIMIT 10
                """;
        return executeSingleLineReport(sql, rs -> rs.getString("first_name") + " " + rs.getString("last_name") + " | emprunts: " + rs.getInt("total"));
    }

    public List<String> availableCopiesPerBookReport() throws SQLException {
        String sql = """
                SELECT b.title,
                       SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies,
                       COUNT(*) AS total_copies
                FROM books b
                LEFT JOIN book_copies bc ON bc.book_id = b.book_id
                GROUP BY b.book_id, b.title
                ORDER BY b.title
                """;
        return executeSingleLineReport(sql,
                rs -> rs.getString("title") + " | disponibles: " + rs.getInt("available_copies") + "/" + rs.getInt("total_copies"));
    }

    private List<String> executeSingleLineReport(String sql, RowFormatter formatter) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> lines = new ArrayList<>();
            while (resultSet.next()) {
                lines.add(formatter.format(resultSet));
            }
            return lines;
        }
    }

    @FunctionalInterface
    private interface RowFormatter {
        String format(ResultSet resultSet) throws SQLException;
    }
}
