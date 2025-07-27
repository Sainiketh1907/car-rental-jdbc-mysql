package com.example.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reservation(
        long reservationId,
        long customerId,
        long carId,
        LocalDate startDate,
        LocalDate endDate,
        Long pickupLocationId,
        Long returnLocationId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {}
