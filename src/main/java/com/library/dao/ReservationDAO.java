package com.library.dao;

import com.library.enums.ReservationStatus;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
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

public class ReservationDAO implements GenericDAO<Reservation, Integer> {
    @Override
    public Optional<Reservation> findById(Integer id) throws SQLException {
        String sql = """
                SELECT r.reservation_id, r.reservation_date, r.expiry_date, r.status,
                       b.book_id, b.title, b.isbn,
                       m.member_id, m.membership_number, u.first_name, u.last_name, u.username
                FROM reservations r
                JOIN books b ON b.book_id = r.book_id
                JOIN members m ON m.member_id = r.member_id
                JOIN users u ON u.user_id = m.member_id
                WHERE r.reservation_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapReservation(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = """
                SELECT r.reservation_id, r.reservation_date, r.expiry_date, r.status,
                       b.book_id, b.title, b.isbn,
                       m.member_id, m.membership_number, u.first_name, u.last_name, u.username
                FROM reservations r
                JOIN books b ON b.book_id = r.book_id
                JOIN members m ON m.member_id = r.member_id
                JOIN users u ON u.user_id = m.member_id
                ORDER BY r.reservation_date DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Reservation> reservations = new ArrayList<>();
            while (resultSet.next()) {
                reservations.add(mapReservation(resultSet));
            }
            return reservations;
        }
    }

    public List<Reservation> findPendingByBookId(int bookId) throws SQLException {
        String sql = """
                SELECT r.reservation_id, r.reservation_date, r.expiry_date, r.status,
                       b.book_id, b.title, b.isbn,
                       m.member_id, m.membership_number, u.first_name, u.last_name, u.username
                FROM reservations r
                JOIN books b ON b.book_id = r.book_id
                JOIN members m ON m.member_id = r.member_id
                JOIN users u ON u.user_id = m.member_id
                WHERE r.book_id = ? AND r.status = 'PENDING'
                ORDER BY r.reservation_date
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
                return reservations;
            }
        }
    }

    public List<Reservation> findByMemberId(int memberId) throws SQLException {
        String sql = """
                SELECT r.reservation_id, r.reservation_date, r.expiry_date, r.status,
                       b.book_id, b.title, b.isbn,
                       m.member_id, m.membership_number, u.first_name, u.last_name, u.username
                FROM reservations r
                JOIN books b ON b.book_id = r.book_id
                JOIN members m ON m.member_id = r.member_id
                JOIN users u ON u.user_id = m.member_id
                WHERE r.member_id = ?
                ORDER BY r.reservation_date DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
                return reservations;
            }
        }
    }

    public boolean existsPendingForMemberAndBook(int memberId, int bookId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM reservations
                WHERE member_id = ? AND book_id = ? AND status = 'PENDING'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setInt(2, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public Reservation save(Reservation entity) throws SQLException {
        String sql = """
                INSERT INTO reservations (book_id, member_id, reservation_date, expiry_date, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, entity.getBook().getBookId());
            statement.setInt(2, entity.getMember().getId());
            statement.setDate(3, Date.valueOf(entity.getReservationDate()));
            statement.setDate(4, entity.getExpiryDate() != null ? Date.valueOf(entity.getExpiryDate()) : null);
            statement.setString(5, entity.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setReservationId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(Reservation entity) throws SQLException {
        String sql = """
                UPDATE reservations
                SET reservation_date = ?, expiry_date = ?, status = ?
                WHERE reservation_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(entity.getReservationDate()));
            statement.setDate(2, entity.getExpiryDate() != null ? Date.valueOf(entity.getExpiryDate()) : null);
            statement.setString(3, entity.getStatus().name());
            statement.setInt(4, entity.getReservationId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void cancel(int reservationId) throws SQLException {
        updateStatus(reservationId, ReservationStatus.CANCELLED);
    }

    public void fulfill(int reservationId) throws SQLException {
        updateStatus(reservationId, ReservationStatus.FULFILLED);
    }

    public void expire(int reservationId) throws SQLException {
        updateStatus(reservationId, ReservationStatus.EXPIRED);
    }

    private void updateStatus(int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, reservationId);
            statement.executeUpdate();
        }
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setBookId(resultSet.getInt("book_id"));
        book.setTitle(resultSet.getString("title"));
        book.setIsbn(resultSet.getString("isbn"));

        Member member = new Member();
        member.setId(resultSet.getInt("member_id"));
        member.setMembershipNumber(resultSet.getString("membership_number"));
        member.setFirstName(resultSet.getString("first_name"));
        member.setLastName(resultSet.getString("last_name"));
        member.setUsername(resultSet.getString("username"));

        return new Reservation(
                resultSet.getInt("reservation_id"),
                book,
                member,
                resultSet.getDate("reservation_date").toLocalDate(),
                resultSet.getDate("expiry_date") != null ? resultSet.getDate("expiry_date").toLocalDate() : null,
                ReservationStatus.valueOf(resultSet.getString("status"))
        );
    }
}
