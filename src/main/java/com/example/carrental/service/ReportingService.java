package com.example.carrental.service;

import com.example.carrental.dao.ReportingDao;

import java.math.BigDecimal;

public class ReportingService {
    private final ReportingDao reportingDao;

    public ReportingService(ReportingDao reportingDao) {
        this.reportingDao = reportingDao;
    }

    public BigDecimal fleetUtilizationRate(int year, int month) {
        return reportingDao.fleetUtilizationRate(year, month);
    }
}
