package com.example.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceSchedule(
        long scheduleId,
        long carId,
        String maintenanceType,
        LocalDate scheduledDate,
        String status,
        BigDecimal estimatedCost
) {}
