SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS maintenance_records;
DROP TABLE IF EXISTS maintenance_schedule;
DROP TABLE IF EXISTS rental_transactions;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS car_category;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS technicians;
DROP TABLE IF EXISTS locations;

SET FOREIGN_KEY_CHECKS = 1;

-- Locations
CREATE TABLE locations (
  location_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  address       VARCHAR(255),
  city          VARCHAR(100),
  latitude      DECIMAL(9,6),
  longitude     DECIMAL(9,6)
);
CREATE INDEX idx_location_city ON locations(city);

-- Customers
CREATE TABLE customers (
  customer_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(120) UNIQUE NOT NULL,
  phone_number  VARCHAR(30),
  licence_number VARCHAR(50) UNIQUE NOT NULL
);
CREATE INDEX idx_customer_email ON customers(email);

-- Categories
CREATE TABLE categories (
  category_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  description   TEXT
);
CREATE INDEX idx_category_name ON categories(name);

-- Cars
CREATE TABLE cars (
  car_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  vin           VARCHAR(50) UNIQUE NOT NULL,
  make          VARCHAR(50) NOT NULL,
  model         VARCHAR(50) NOT NULL,
  year          INT NOT NULL,
  color         VARCHAR(30),
  status        ENUM('available','maintenance','out_of_service') DEFAULT 'available',
  location_id   BIGINT,
  CONSTRAINT fk_car_location FOREIGN KEY (location_id) REFERENCES locations(location_id) ON DELETE SET NULL
);
CREATE INDEX idx_cars_location_status ON cars(location_id, status);
CREATE INDEX idx_cars_make_model ON cars(make, model);

-- Car Category
CREATE TABLE car_category (
  car_id      BIGINT,
  category_id BIGINT,
  assigned_date DATE DEFAULT (CURRENT_DATE),
  is_active   BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (car_id, category_id, assigned_date),
  CONSTRAINT fk_cc_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE,
  CONSTRAINT fk_cc_cat FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);
CREATE INDEX idx_car_category_active ON car_category(category_id, is_active);

-- Reservations
CREATE TABLE reservations (
  reservation_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id      BIGINT NOT NULL,
  car_id           BIGINT NOT NULL,
  start_date       DATE NOT NULL,
  end_date         DATE NOT NULL,
  pickup_location_id BIGINT,
  return_location_id BIGINT,
  status           ENUM('reserved','active','completed','cancelled') DEFAULT 'reserved',
  total_amount     DECIMAL(12,2) NOT NULL,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_res_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  CONSTRAINT fk_res_car FOREIGN KEY (car_id) REFERENCES cars(car_id),
  CONSTRAINT fk_res_pickup FOREIGN KEY (pickup_location_id) REFERENCES locations(location_id),
  CONSTRAINT fk_res_return FOREIGN KEY (return_location_id) REFERENCES locations(location_id)
);
CREATE INDEX idx_res_customer_date ON reservations(customer_id, created_at);
CREATE INDEX idx_res_car_date ON reservations(car_id, start_date, end_date);

-- Rental transactions
CREATE TABLE rental_transactions (
  transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  pickup_time    DATETIME,
  return_time    DATETIME,
  final_amount   DECIMAL(12,2),
  CONSTRAINT fk_tx_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE
);

-- Maintenance schedule
CREATE TABLE maintenance_schedule (
  schedule_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  car_id         BIGINT NOT NULL,
  maintenance_type VARCHAR(100) NOT NULL,
  scheduled_date DATE NOT NULL,
  status         ENUM('scheduled','completed','cancelled','overdue') DEFAULT 'scheduled',
  estimated_cost DECIMAL(12,2),
  CONSTRAINT fk_ms_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE
);
CREATE INDEX idx_ms_car_status_date ON maintenance_schedule(car_id, status, scheduled_date);

-- Technicians
CREATE TABLE technicians (
  technician_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  phone         VARCHAR(30),
  specialization VARCHAR(100)
);

-- Maintenance records
CREATE TABLE maintenance_records (
  record_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  schedule_id    BIGINT,
  car_id         BIGINT NOT NULL,
  technician_id  BIGINT,
  completed_date DATE NOT NULL,
  work_description TEXT,
  actual_cost    DECIMAL(12,2),
  CONSTRAINT fk_mr_schedule FOREIGN KEY (schedule_id) REFERENCES maintenance_schedule(schedule_id) ON DELETE SET NULL,
  CONSTRAINT fk_mr_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE,
  CONSTRAINT fk_mr_tech FOREIGN KEY (technician_id) REFERENCES technicians(technician_id)
);
CREATE INDEX idx_mr_car_completed_date ON maintenance_records(car_id, completed_date);
