package com.example.carrental.model;

public record Car(
        long carId,
        String vin,
        String make,
        String model,
        int year,
        String color,
        String status,
        Long locationId
) {}
