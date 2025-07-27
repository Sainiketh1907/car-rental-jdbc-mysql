package com.example.carrental.dao.impl;

import com.example.carrental.dao.LocationDao;
import com.example.carrental.model.Location;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLocationDao implements LocationDao {

    private final DataSource ds;

    public JdbcLocationDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<Location> findAll() {
        final String sql = "SELECT * FROM locations ORDER BY location_id";
        List<Location> list = new ArrayList<>();
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
    public Optional<Location> findById(long id) {
        final String sql = "SELECT * FROM locations WHERE location_id = ?";
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
    public long create(Location l) {
        final String sql = "INSERT INTO locations (name, address, city, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.name());
            ps.setString(2, l.address());
            ps.setString(3, l.city());
            if (l.latitude() == null) ps.setNull(4, Types.DECIMAL); else ps.setBigDecimal(4, java.math.BigDecimal.valueOf(l.latitude()));
            if (l.longitude() == null) ps.setNull(5, Types.DECIMAL); else ps.setBigDecimal(5, java.math.BigDecimal.valueOf(l.longitude()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("No generated key");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Location map(ResultSet rs) throws SQLException {
        return new Location(
                rs.getLong("location_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("city"),
                (rs.getObject("latitude") == null) ? null : rs.getBigDecimal("latitude").doubleValue(),
                (rs.getObject("longitude") == null) ? null : rs.getBigDecimal("longitude").doubleValue()
        );
    }
}
