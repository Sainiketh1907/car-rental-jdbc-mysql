package com.example.carrental.model;

public record Technician(
        long technicianId,
        String name,
        String phone,
        String specialization
) {}
