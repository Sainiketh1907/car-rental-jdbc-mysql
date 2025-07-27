package com.example.carrental.dao.impl;

import com.example.carrental.dao.CustomerDao;
import com.example.carrental.model.Customer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JdbcCustomerDao implements CustomerDao {

    private final DataSource ds;

    public JdbcCustomerDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public long create(Customer c) {
        final String sql = "INSERT INTO customers (name, email, phone_number, licence_number) VALUES (?, ?, ?, ?)";
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.name());
            ps.setString(2, c.email());
            ps.setString(3, c.phoneNumber());
            ps.setString(4, c.licenceNumber());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("No generated key");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Customer> findByLicence(String licence) {
        final String sql = "SELECT * FROM customers WHERE licence_number=?";
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, licence);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Customer(
                            rs.getLong("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("licence_number")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
