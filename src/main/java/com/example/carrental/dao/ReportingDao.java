package com.example.carrental.dao;

import java.math.BigDecimal;

public interface ReportingDao {
    BigDecimal fleetUtilizationRate(int year, int month);
}
