package com.example.carrental.dao.impl;

import com.example.carrental.dao.ReportingDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

public class JdbcReportingDao implements ReportingDao {

    private final DataSource ds;

    public JdbcReportingDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public BigDecimal fleetUtilizationRate(int year, int month) {
        final String monthStart = String.format("%04d-%02d-01", year, month);
        final String sql = """
            WITH params AS (
              SELECT DATE(?) AS start_d,
                     LAST_DAY(DATE(?)) AS end_d
            ),
            fleet AS (SELECT COUNT(*) AS fleet_size FROM cars),
            reserved AS (
              SELECT SUM(DATEDIFF(LEAST(r.end_date, p.end_d),
                                   GREATEST(r.start_date, p.start_d)) + 1) AS reserved_days
              FROM reservations r
              JOIN params p
              WHERE r.status IN ('reserved','active','completed')
                AND r.start_date <= p.end_d
                AND r.end_date >= p.start_d
            )
            SELECT (reserved_days / (fleet_size * DAY(LAST_DAY(?)))) AS utilization
            FROM reserved, fleet;
        """;
        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, monthStart);
            ps.setString(2, monthStart);
            ps.setString(3, monthStart);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("utilization");
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
