package com.example.carrental.dao;

import com.example.carrental.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    List<Category> findAll();
    Optional<Category> findById(long id);
    long create(Category c);
}

