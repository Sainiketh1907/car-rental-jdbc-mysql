package com.example.carrental.dao;

import java.time.LocalDate;
import java.util.List;

import com.example.carrental.model.Car;

public interface CarDao {
    List<Car> findAll();

    List<Car> findAvailableCars(long locationId, long categoryId, LocalDate start, LocalDate end);

    List<Car> findAvailableCarsNear(double lat, double lon, double radiusKm, long categoryId,
                                    LocalDate start, LocalDate end);
}
