package com.example.carrental.model;

public record Customer(
        long customerId,
        String name,
        String email,
        String phoneNumber,
        String licenceNumber
) {}
