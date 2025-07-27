package com.example.carrental.service;

import com.example.carrental.dao.ReservationDao;
import com.example.carrental.model.Reservation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ReservationService {
    private final DataSource ds;
    private final ReservationDao reservationDao;

    public ReservationService(DataSource ds, ReservationDao reservationDao) {
        this.ds = ds;
        this.reservationDao = reservationDao;
    }

    // Create a reservation with overlap check (transactional)
    public long makeReservation(Reservation r) throws SQLException {
        try (Connection c = ds.getConnection()) {
            boolean oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                if (reservationDao.existsOverlapping(r.carId(), r.startDate(), r.endDate())) {
                    throw new IllegalStateException("Car is already booked for those dates");
                }
                long id = reservationDao.create(r);
                c.commit();
                return id;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(oldAutoCommit);
            }
        }
    }
}
