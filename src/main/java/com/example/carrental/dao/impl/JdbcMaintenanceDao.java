package com.example.carrental.dao.impl;

import com.example.carrental.dao.MaintenanceDao;
import com.example.carrental.model.Car;
import com.example.carrental.model.MaintenanceSchedule;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcMaintenanceDao implements MaintenanceDao {

    private final DataSource ds;

    public JdbcMaintenanceDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<MaintenanceSchedule> upcomingMaintenance(long carId, LocalDate from, LocalDate to) {
        final String sql = """
          SELECT * FROM maintenance_schedule
          WHERE car_id = ? AND scheduled_date BETWEEN ? AND ? AND status = 'scheduled'
          ORDER BY scheduled_date
          """;
        List<MaintenanceSchedule> list = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, carId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSchedule(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Car> carsOverdueForMaintenance(LocalDate today) {
        final String sql = """
          SELECT DISTINCT c.*
          FROM cars c
          JOIN maintenance_schedule ms ON ms.car_id = c.car_id
          WHERE ms.status IN ('scheduled','overdue')
            AND ms.scheduled_date < ?
            AND NOT EXISTS (
               SELECT 1 FROM maintenance_records mr
               WHERE mr.schedule_id = ms.schedule_id
            )
          """;
        List<Car> res = new ArrayList<>();
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(new Car(
                            rs.getLong("car_id"),
                            rs.getString("vin"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getString("color"),
                            rs.getString("status"),
                            (Long) rs.getObject("location_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private MaintenanceSchedule mapSchedule(ResultSet rs) throws SQLException {
        return new MaintenanceSchedule(
                rs.getLong("schedule_id"),
                rs.getLong("car_id"),
                rs.getString("maintenance_type"),
                rs.getDate("scheduled_date").toLocalDate(),
                rs.getString("status"),
                rs.getBigDecimal("estimated_cost")
        );
    }
}
