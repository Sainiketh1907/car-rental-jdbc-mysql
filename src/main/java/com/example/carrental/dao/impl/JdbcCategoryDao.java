package com.example.carrental.dao.impl;

import com.example.carrental.dao.CategoryDao;
import com.example.carrental.model.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryDao implements CategoryDao {

    private final DataSource ds;

    public JdbcCategoryDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<Category> findAll() {
        final String sql = "SELECT * FROM categories ORDER BY category_id";
        List<Category> list = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<Category> findById(long id) {
        final String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long create(Category c) {
        final String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.name());
            ps.setString(2, c.description());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("No generated key");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("category_id"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}
