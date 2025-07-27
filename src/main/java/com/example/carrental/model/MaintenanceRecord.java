package com.example.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceRecord(
        long recordId,
        Long scheduleId,
        long carId,
        Long technicianId,
        LocalDate completedDate,
        String workDescription,
        BigDecimal actualCost
) {}
