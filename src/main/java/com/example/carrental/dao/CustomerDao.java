package com.example.carrental.dao;

import com.example.carrental.model.Customer;
import java.util.Optional;

public interface CustomerDao {
    long create(Customer c);
    Optional<Customer> findByLicence(String licence);
}
