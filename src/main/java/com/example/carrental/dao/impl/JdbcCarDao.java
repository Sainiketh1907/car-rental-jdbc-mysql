package com.example.carrental.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.example.carrental.dao.CarDao;
import com.example.carrental.model.Car;

public class JdbcCarDao implements CarDao {
    private final DataSource ds;

    public JdbcCarDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<Car> findAvailableCars(long locationId, long categoryId, LocalDate start, LocalDate end) {
        final String sql = """
          SELECT c.*
          FROM cars c
          JOIN car_category cc ON cc.car_id = c.car_id AND cc.is_active = TRUE
          WHERE c.location_id = ?
            AND cc.category_id = ?
            AND c.status = 'available'
            AND NOT EXISTS (
                SELECT 1 FROM reservations r
                WHERE r.car_id = c.car_id
                  AND r.status IN ('reserved','active')
                  AND NOT (r.end_date < ? OR r.start_date > ?)
            )
          """;
        List<Car> cars = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, locationId);
            ps.setLong(2, categoryId);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cars.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cars;
    }

    @Override
    public List<Car> findAvailableCarsNear(double lat, double lon, double radiusKm, long categoryId,
                                           LocalDate start, LocalDate end) {
        final String sql = """
          SELECT c.*, 
            (6371 * acos(
              cos(radians(?)) * cos(radians(l.latitude)) *
              cos(radians(l.longitude) - radians(?)) +
              sin(radians(?)) * sin(radians(l.latitude))
            )) AS distance_km
          FROM cars c
          JOIN locations l ON l.location_id = c.location_id
          JOIN car_category cc ON cc.car_id = c.car_id AND cc.is_active = TRUE
          WHERE cc.category_id = ?
            AND c.status = 'available'
            AND NOT EXISTS (
                SELECT 1 FROM reservations r
                WHERE r.car_id = c.car_id
                  AND r.status IN ('reserved','active')
                  AND NOT (r.end_date < ? OR r.start_date > ?)
            )
          HAVING distance_km <= ?
          ORDER BY distance_km ASC
          """;
        List<Car> cars = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setDouble(i++, lat);
            ps.setDouble(i++, lon);
            ps.setDouble(i++, lat);
            ps.setLong(i++, categoryId);
            ps.setDate(i++, Date.valueOf(start));
            ps.setDate(i++, Date.valueOf(end));
            ps.setDouble(i++, radiusKm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) cars.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cars;
    }

    private Car map(ResultSet rs) throws SQLException {
        return new Car(
                rs.getLong("car_id"),
                rs.getString("vin"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color"),
                rs.getString("status"),
                (Long) rs.getObject("location_id")
        );
    }

    @Override
    public List<Car> findAll() {
        final String sql = "SELECT * FROM cars";
        List<Car> cars = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cars.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cars;
    }
}
