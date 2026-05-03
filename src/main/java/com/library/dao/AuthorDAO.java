package com.library.dao;

import com.library.model.Author;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthorDAO implements GenericDAO<Author, Integer> {
    @Override
    public Optional<Author> findById(Integer id) throws SQLException {
        String sql = "SELECT author_id, first_name, last_name FROM authors WHERE author_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAuthor(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Author> findAll() throws SQLException {
        String sql = "SELECT author_id, first_name, last_name FROM authors ORDER BY last_name, first_name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Author> authors = new ArrayList<>();
            while (resultSet.next()) {
                authors.add(mapAuthor(resultSet));
            }
            return authors;
        }
    }

    @Override
    public Author save(Author entity) throws SQLException {
        String sql = "INSERT INTO authors (first_name, last_name) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setAuthorId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(Author entity) throws SQLException {
        String sql = "UPDATE authors SET first_name = ?, last_name = ? WHERE author_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setInt(3, entity.getAuthorId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM authors WHERE author_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Author mapAuthor(ResultSet resultSet) throws SQLException {
        return new Author(
                resultSet.getInt("author_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name")
        );
    }
}
