package com.example.carrental;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

import javax.sql.DataSource;

import com.example.carrental.config.DataSourceFactory;
import com.example.carrental.dao.CarDao;
import com.example.carrental.dao.CategoryDao;
import com.example.carrental.dao.CustomerDao;
import com.example.carrental.dao.LocationDao;
import com.example.carrental.dao.ReservationDao;
import com.example.carrental.dao.impl.JdbcCarDao;
import com.example.carrental.dao.impl.JdbcCategoryDao;
import com.example.carrental.dao.impl.JdbcCustomerDao;
import com.example.carrental.dao.impl.JdbcLocationDao;
import com.example.carrental.dao.impl.JdbcReportingDao;
import com.example.carrental.dao.impl.JdbcReservationDao;
import com.example.carrental.model.Car;
import com.example.carrental.model.Customer;
import com.example.carrental.model.Reservation;
import com.example.carrental.service.ReportingService;
import com.example.carrental.service.ReservationService;

public class CarRentalCLI {

    private final Scanner in = new Scanner(System.in);

    private final CarDao carDao;
    private final ReservationDao reservationDao;
    private final CustomerDao customerDao;
    private final LocationDao locationDao;
    private final CategoryDao categoryDao;
    private final ReportingService reportingService;
    private final ReservationService reservationService;

    public CarRentalCLI(DataSource ds) {
        this.carDao = new JdbcCarDao(ds);
        this.reservationDao = new JdbcReservationDao(ds);
        this.customerDao = new JdbcCustomerDao(ds);
        this.locationDao = new JdbcLocationDao(ds);
        this.categoryDao = new JdbcCategoryDao(ds);
        this.reportingService = new ReportingService(new JdbcReportingDao(ds));
        this.reservationService = new ReservationService(ds, reservationDao);
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream in = CarRentalCLI.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        }
        DataSource ds = DataSourceFactory.get(props);

        new CarRentalCLI(ds).run();
    }

    private void run() {
        System.out.println("=== Car Rental & Fleet Management CLI ===");
        while (true) {
            printMenu();
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listAvailableCars();
                    case "2" -> makeReservation();
                    case "3" -> showCustomerHistory();
                    case "4" -> showFleetUtilization();
                    case "5" -> findAvailableCarsNear();
                    case "6" -> listLocations();
                    case "7" -> listCategories();
                    case "8" -> listAllCars();
                    case "0" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("✖ Error: " + e.getMessage());
                e.printStackTrace(System.out);
            }
        }
    }

    private void printMenu() {
        System.out.println("""
                
                ------------------------------------------
                1) Find available cars (by location & category)
                2) Make a reservation (auto create customer if needed)
                3) Show a customer's rental history (by licence)
                4) Fleet utilization rate for a month
                5) Find available cars near coordinates (lat/lon, radius)
                6) List all locations
                7) List all categories
                8) List all cars
                0) Exit
                ------------------------------------------
                Enter choice: """);
    }

    private void listAvailableCars() {
        long locationId = askLong("Location ID: ");
        long categoryId = askLong("Category ID: ");
        LocalDate start = askDate("Start date (yyyy-MM-dd): ");
        LocalDate end = askDate("End date (yyyy-MM-dd): ");

        List<Car> cars = carDao.findAvailableCars(locationId, categoryId, start, end);
        if (cars.isEmpty()) {
            System.out.println("No cars available.");
        } else {
            System.out.println("Available cars:");
            cars.forEach(System.out::println);
        }
    }

    private void listAllCars() {
        List<Car> cars = carDao.findAll();
        if (cars.isEmpty()) {
            System.out.println("No cars found.");
        } else {
            System.out.println("All cars:");
            cars.forEach(System.out::println);
        }
    }

    private void makeReservation() throws Exception {
        System.out.println("Enter customer licence number:");
        String licence = in.nextLine().trim();

        long customerId = ensureCustomer(licence);

        // Show all cars before asking for the car ID
        System.out.println("\n--- All cars (choose a valid car_id) ---");
        listAllCars();

        long carId = askLong("Car ID to reserve: ");
        LocalDate start = askDate("Start date (yyyy-MM-dd): ");
        LocalDate end = askDate("End date (yyyy-MM-dd): ");
        long pickupLoc = askLong("Pickup location ID: ");
        long returnLoc = askLong("Return location ID: ");
        BigDecimal amount = askBigDecimal("Total amount: ");

        Reservation r = new Reservation(
                0,
                customerId,
                carId,
                start,
                end,
                pickupLoc,
                returnLoc,
                "reserved",
                amount,
                LocalDateTime.now()
        );

        try {
            long id = reservationService.makeReservation(r);
            System.out.println("✔ Reservation created with id = " + id);
        } catch (IllegalStateException ise) {
            System.out.println("✖ Overlap error: " + ise.getMessage());
        } catch (RuntimeException re) {
            System.out.println("✖ Database error (check customer_id, car_id, location ids, etc.): " + re.getMessage());
            throw re;
        }
    }

    private void showCustomerHistory() {
        System.out.println("Enter customer licence number:");
        String licence = in.nextLine().trim();
        Optional<Customer> c = customerDao.findByLicence(licence);
        if (c.isEmpty()) {
            System.out.println("No such customer.");
            return;
        }
        long customerId = c.get().customerId();
        System.out.println("Customer: " + c.get());
        reservationDao.findByCustomer(customerId).forEach(System.out::println);
    }

    private void showFleetUtilization() {
        YearMonth ym = askYearMonth("Enter year-month (yyyy-MM): ");
        System.out.println("Utilization for " + ym + ": " +
                reportingService.fleetUtilizationRate(ym.getYear(), ym.getMonthValue()));
    }

    private void findAvailableCarsNear() {
        double lat = askDouble("Latitude: ");
        double lon = askDouble("Longitude: ");
        double radiusKm = askDouble("Radius in km: ");
        long categoryId = askLong("Category ID: ");
        LocalDate start = askDate("Start date (yyyy-MM-dd): ");
        LocalDate end = askDate("End date (yyyy-MM-dd): ");

        List<Car> cars = carDao.findAvailableCarsNear(lat, lon, radiusKm, categoryId, start, end);
        if (cars.isEmpty()) {
            System.out.println("No cars available within " + radiusKm + " km.");
        } else {
            System.out.println("Available cars:");
            cars.forEach(System.out::println);
        }
    }

    private void listLocations() {
        var list = locationDao.findAll();
        if (list.isEmpty()) {
            System.out.println("No locations found.");
        } else {
            System.out.println("Locations:");
            list.forEach(System.out::println);
        }
    }

    private void listCategories() {
        var list = categoryDao.findAll();
        if (list.isEmpty()) {
            System.out.println("No categories found.");
        } else {
            System.out.println("Categories:");
            list.forEach(System.out::println);
        }
    }

    // ---------- Helpers ----------
    private long ensureCustomer(String licence) {
        Optional<Customer> existing = customerDao.findByLicence(licence);
        if (existing.isPresent()) {
            System.out.println("Customer found: " + existing.get());
            return existing.get().customerId();
        }
        System.out.println("No customer found with that licence. Creating one...");
        String name = ask("Name: ");
        String email = ask("Email: ");
        String phone = ask("Phone: ");
        Customer c = new Customer(0, name, email, phone, licence);
        long id = customerDao.create(c);
        System.out.println("Customer created with id=" + id);
        return id;
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private long askLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private double askDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private BigDecimal askBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Try again.");
            }
        }
    }

    private LocalDate askDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Expected yyyy-MM-dd.");
            }
        }
    }

    private YearMonth askYearMonth(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                return YearMonth.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid month. Expected yyyy-MM.");
            }
        }
    }
}
