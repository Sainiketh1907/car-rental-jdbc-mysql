package com.example.carrental.model;

public record Location(
        long locationId,
        String name,
        String address,
        String city,
        Double latitude,
        Double longitude
) {}
