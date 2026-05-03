package com.library.dao;

import com.library.model.Category;
import com.library.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAO implements GenericDAO<Category, Integer> {
    @Override
    public Optional<Category> findById(Integer id) throws SQLException {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategory(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Category> findByName(String name) throws SQLException {
        String sql = "SELECT category_id, name, description FROM categories WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategory(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, name, description FROM categories ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }
            return categories;
        }
    }

    @Override
    public Category save(Category entity) throws SQLException {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setCategoryId(keys.getInt(1));
                }
            }
            return entity;
        }
    }

    @Override
    public void update(Category entity) throws SQLException {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getDescription());
            statement.setInt(3, entity.getCategoryId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Category mapCategory(ResultSet resultSet) throws SQLException {
        return new Category(
                resultSet.getInt("category_id"),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
