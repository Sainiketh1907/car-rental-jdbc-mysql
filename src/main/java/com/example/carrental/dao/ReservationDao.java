package com.example.carrental.dao;

import com.example.carrental.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    Optional<Reservation> findById(long id);
    long create(Reservation r);
    void updateStatus(long reservationId, String status);
    boolean existsOverlapping(long carId, LocalDate start, LocalDate end);
    List<Reservation> findByCustomer(long customerId);
}
