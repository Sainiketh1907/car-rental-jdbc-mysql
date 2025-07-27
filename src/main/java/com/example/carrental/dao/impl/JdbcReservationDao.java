package com.example.carrental.dao.impl;

import com.example.carrental.dao.ReservationDao;
import com.example.carrental.model.Reservation;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReservationDao implements ReservationDao {

    private final DataSource ds;

    public JdbcReservationDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Optional<Reservation> findById(long id) {
        final String sql = "SELECT * FROM reservations WHERE reservation_id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
    public long create(Reservation r) {
        final String sql = "INSERT INTO reservations (customer_id, car_id, start_date, end_date, pickup_location_id, return_location_id, status, total_amount, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            ps.setLong(i++, r.customerId());
            ps.setLong(i++, r.carId());
            ps.setDate(i++, Date.valueOf(r.startDate()));
            ps.setDate(i++, Date.valueOf(r.endDate()));
            if (r.pickupLocationId() == null) ps.setNull(i++, Types.BIGINT); else ps.setLong(i++, r.pickupLocationId());
            if (r.returnLocationId() == null) ps.setNull(i++, Types.BIGINT); else ps.setLong(i++, r.returnLocationId());
            ps.setString(i++, r.status());
            ps.setBigDecimal(i++, r.totalAmount());
            ps.setTimestamp(i++, Timestamp.valueOf(r.createdAt()));

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
    public void updateStatus(long reservationId, String status) {
        final String sql = "UPDATE reservations SET status=? WHERE reservation_id=?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsOverlapping(long carId, LocalDate start, LocalDate end) {
        final String sql = """
            SELECT 1 FROM reservations
            WHERE car_id = ?
              AND status IN ('reserved','active')
              AND NOT (end_date < ? OR start_date > ?)
            LIMIT 1
            """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, carId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Reservation> findByCustomer(long customerId) {
        final String sql = "SELECT * FROM reservations WHERE customer_id=? ORDER BY created_at DESC";
        List<Reservation> res = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getLong("reservation_id"),
                rs.getLong("customer_id"),
                rs.getLong("car_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                (Long) rs.getObject("pickup_location_id"),
                (Long) rs.getObject("return_location_id"),
                rs.getString("status"),
                rs.getBigDecimal("total_amount"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
