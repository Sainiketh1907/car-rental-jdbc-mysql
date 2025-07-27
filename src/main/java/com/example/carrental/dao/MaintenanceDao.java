package com.example.carrental.dao;

import com.example.carrental.model.Car;
import com.example.carrental.model.MaintenanceSchedule;
import java.time.LocalDate;
import java.util.List;

public interface MaintenanceDao {
    List<MaintenanceSchedule> upcomingMaintenance(long carId, LocalDate from, LocalDate to);
    List<Car> carsOverdueForMaintenance(LocalDate today);
}
