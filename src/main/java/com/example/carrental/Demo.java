package com.example.carrental;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import com.example.carrental.config.DataSourceFactory;
import com.example.carrental.dao.CarDao;
import com.example.carrental.dao.CustomerDao;
import com.example.carrental.dao.ReportingDao;
import com.example.carrental.dao.ReservationDao;
import com.example.carrental.dao.impl.JdbcCarDao;
import com.example.carrental.dao.impl.JdbcCustomerDao;
import com.example.carrental.dao.impl.JdbcReportingDao;
import com.example.carrental.dao.impl.JdbcReservationDao;
import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.ReportingService;
import com.example.carrental.service.ReservationService;

public class Demo {

    public static void main(String[] args) throws Exception {
        // Load DB props
        Properties props = new Properties();
        try (InputStream in = Demo.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        }
        DataSource ds = DataSourceFactory.get(props);

        // DAOs & Services
        CarDao carDao = new JdbcCarDao(ds);
        ReservationDao reservationDao = new JdbcReservationDao(ds);
        CustomerDao customerDao = new JdbcCustomerDao(ds);
        ReportingDao reportingDao = new JdbcReportingDao(ds);
        ReservationService reservationService = new ReservationService(ds, reservationDao);
        ReportingService reportingService = new ReportingService(reportingDao);

        // -----------------------------
        // 1) List available SUVs @ Main Branch for given window
        // -----------------------------
        LocalDate start = LocalDate.of(2025, 8, 1);
        LocalDate end   = LocalDate.of(2025, 8, 5);
        List<Car> availableSUVs = carDao.findAvailableCars(1L, 1L, start, end);
        System.out.println("Available SUVs in Main Branch (loc=1, cat=1) between " + start + " and " + end + ":");
        availableSUVs.forEach(System.out::println);

        // -----------------------------
        // 2) Show customer 1 history
        // -----------------------------
        System.out.println("\nReservation history for customer 1:");
        reservationDao.findByCustomer(1L).forEach(System.out::println);

        // -----------------------------
        // 3) Try overlapping reservation (should fail)
        // -----------------------------
        Reservation conflicting = new Reservation(
                0, 2, 1,
                LocalDate.of(2025, 8, 3),
                LocalDate.of(2025, 8, 4),
                1L, 1L,
                "reserved",
                new BigDecimal("1500.00"),
                LocalDateTime.now()
        );
        try {
            reservationService.makeReservation(conflicting);
            System.out.println("Unexpected: overlapping reservation succeeded!");
        } catch (IllegalStateException e) {
            System.out.println("\nExpected conflict: " + e.getMessage());
        }

        // -----------------------------
        // 4) Find-or-create a customer, then reserve a car for them
        // -----------------------------
        String licence = "DL-4444"; // change if you want to reuse another licence
        long customerId = ensureCustomer(customerDao,
                new Customer(0, "Fiona Das", "fiona@example.com", "4444444444", licence));

        // Make sure the car exists & is not overlapping in that period (car_id=2 here)
        Reservation newRes = new Reservation(
                0, customerId, 2,
                LocalDate.of(2025, 8, 25),
                LocalDate.of(2025, 8, 28),
                1L, 1L,
                "reserved",
                new BigDecimal("4000.00"),
                LocalDateTime.now()
        );
        try {
            long resId = reservationService.makeReservation(newRes);
            System.out.println("\nNew reservation created with ID: " + resId);
        } catch (IllegalStateException ise) {
            System.out.println("\nReservation failed (overlap): " + ise.getMessage());
        } catch (RuntimeException re) {
            System.out.println("\nReservation failed (FK or other DB error): " + re.getMessage());
            throw re;
        }

        // -----------------------------
        // 5) Fleet utilization
        // -----------------------------
        System.out.println("\nFleet utilization for 2025-08: " + reportingService.fleetUtilizationRate(2025, 8));
    }

    /**
     * Ensures a customer exists: look up by licence; if missing, create and return the new id.
     */
    private static long ensureCustomer(CustomerDao customerDao, Customer proto) {
        Optional<Customer> existing = customerDao.findByLicence(proto.licenceNumber());
        if (existing.isPresent()) {
            System.out.println("\nCustomer already exists: " + existing.get());
            return existing.get().customerId();
        }
        long id = customerDao.create(proto);
        System.out.println("\nCreated new customer with id=" + id);
        return id;
    }
}
