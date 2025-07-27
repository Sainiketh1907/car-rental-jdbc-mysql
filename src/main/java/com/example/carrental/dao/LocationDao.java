package com.example.carrental.dao;

import com.example.carrental.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationDao {
    List<Location> findAll();
    Optional<Location> findById(long id);
    long create(Location l);
}
