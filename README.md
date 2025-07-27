# Car Rental Management System

A Java-based car rental management system using JDBC with MySQL database and HikariCP connection pooling.

## Features

- **Customer Management**: Register customers with license verification
- **Car Fleet Management**: Track cars, categories, and locations
- **Reservation System**: Handle bookings with date validation
- **Rental Transactions**: Process pickups and returns
- **Maintenance Scheduling**: Track car maintenance and technicians
- **Database Integration**: MySQL with optimized connection pooling

## Technologies Used

- **Java 17**: Modern Java features and performance
- **MySQL 8.0**: Relational database with advanced features
- **JDBC**: Direct database connectivity
- **HikariCP**: High-performance connection pooling
- **Maven**: Build automation and dependency management
- **JUnit 5**: Unit testing framework
- **SLF4J**: Logging framework

## Project Structure

```
car-rental-jdbc-mysql/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/carrental/
│   │   │       ├── App.java
│   │   │       └── config/
│   │   │           └── DataSourceFactory.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
├── pom.xml
└── README.md
```

## Database Schema

The system includes the following main entities:

- **Locations**: Car pickup/return locations
- **Customers**: Customer information and license details
- **Categories**: Car categories (economy, luxury, SUV, etc.)
- **Cars**: Vehicle inventory with status tracking
- **Reservations**: Booking management with status workflow
- **Rental Transactions**: Actual pickup/return records
- **Maintenance**: Scheduled and completed maintenance records
- **Technicians**: Maintenance staff information

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0** or higher
- **Git** (optional)

## Database Setup

### 1. Install MySQL

Download and install MySQL from [official website](https://dev.mysql.com/downloads/)

### 2. Create Database and User

```sql
-- Connect as root user
CREATE DATABASE car_rental;

-- Create dedicated user
CREATE USER 'car_user'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON car_rental.* TO 'car_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Database Connection

Update `src/main/resources/application.properties` if needed:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/car_rental?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
db.username=car_user
db.password=secret

# HikariCP Configuration
hikari.maximumPoolSize=10
hikari.connectionTimeout=30000
hikari.idleTimeout=600000
hikari.maxLifetime=1800000
```

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Sainiketh1907/car-rental-jdbc-mysql.git
cd car-rental-jdbc-mysql
```

### 2. Initialize Database

Run the SQL Maven plugin to create schema and load sample data:

```bash
mvn clean compile sql:execute
```

This will:
- Create all necessary tables
- Set up foreign key relationships
- Load sample data (if data.sql exists)

### 3. Compile the Project

```bash
mvn clean compile
```

### 4. Run Tests

```bash
mvn test
```

## Running the Application

### Method 1: Using Maven Exec Plugin

```bash
mvn exec:java
```

### Method 2: Using Fat JAR

Build the fat JAR:
```bash
mvn clean package
```

--------------------------------IMPORTANT---------------------------------


Run the application:
```bash
java -jar target/car-rental-jdbc-mysql-1.0-SNAPSHOT-all.jar
```

--------------------------------------------------------------------------'

### Method 3: Direct Java Execution

```bash
mvn clean compile
java -cp target/classes:target/dependency/* com.example.carrental.CarRentalCLI
```

## Development

### Building

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package (creates fat JAR)
mvn package

# Clean everything
mvn clean
```

### Database Management

```bash
# Reset database schema
mvn sql:execute

# Skip tests during build
mvn package -DskipTests
```

### IDE Setup

1. Import as Maven project
2. Set Java SDK to 17+
3. Configure database connection in application.properties
4. Run/Debug main class: `com.example.carrental.CarRentalCLI`

## Configuration

### Database Properties

The application supports the following configuration properties:

```properties
# Required Database Settings
db.url=jdbc:mysql://localhost:3306/car_rental
db.username=car_user
db.password=secret

# Optional HikariCP Settings
hikari.maximumPoolSize=10
hikari.connectionTimeout=30000
hikari.idleTimeout=600000
hikari.maxLifetime=1800000
hikari.minimumIdle=5
```

### Environment Variables

You can also use environment variables:
```bash
export DB_URL="jdbc:mysql://localhost:3306/car_rental"
export DB_USERNAME="car_user"
export DB_PASSWORD="secret"
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   ```
   Solution: Ensure MySQL is running on localhost:3306
   Check: systemctl status mysql (Linux) or services.msc (Windows)
   ```

2. **Access Denied**
   ```
   Solution: Verify username/password and user privileges
   Check: GRANT ALL PRIVILEGES ON car_rental.* TO 'car_user'@'localhost';
   ```

3. **Table Doesn't Exist**
   ```
   Solution: Run database initialization
   Command: mvn sql:execute
   ```

4. **Java Version Issues**
   ```
   Solution: Ensure Java 17+ is installed and JAVA_HOME is set
   Check: java -version
   ```

### Logging

The application uses SLF4J with simple logger. Logs will appear in the console.

To enable debug logging, add to application.properties:
```properties
org.slf4j.simpleLogger.defaultLogLevel=debug
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact [sainiketh1907@gmail.com]

---

**Note**: This is a demo project for educational purposes. For production use, consider additional security measures, error handling, and monitoring.
