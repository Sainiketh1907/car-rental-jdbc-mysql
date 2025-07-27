package com.example.carrental;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.carrental.config.DataSourceFactory;
import com.example.carrental.dao.ReservationDao;
import com.example.carrental.dao.impl.JdbcReservationDao;
import com.example.carrental.model.Reservation;

public class ReservationDaoIT {

    static DataSource ds;
    static ReservationDao reservationDao;

    @BeforeAll
    static void setup() throws Exception {
        Properties props = new Properties();
        try (InputStream in = ReservationDaoIT.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        }
        ds = DataSourceFactory.get(props);
        reservationDao = new JdbcReservationDao(ds);
    }

    @Test
    void testOverlap() {
        boolean overlap = reservationDao.existsOverlapping(1L,
                LocalDate.parse("2025-08-03"),
                LocalDate.parse("2025-08-04"));
        assertTrue(overlap);

        boolean noOverlap = reservationDao.existsOverlapping(1L,
                LocalDate.parse("2025-08-10"),
                LocalDate.parse("2025-08-12"));
        assertFalse(noOverlap);
    }

    @Test
    void testCreate() {
        Reservation r = new Reservation(
                0, 1, 2,
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-09-03"),
                1L, 1L,
                "reserved",
                new BigDecimal("500.00"),
                LocalDateTime.now()
        );
        long id = reservationDao.create(r);
        assertTrue(id > 0);
    }
}
