INSERT INTO locations (name, address, city, latitude, longitude)
VALUES
('Main Branch', '123 Central Ave', 'Hyderabad', 17.3850, 78.4867),
('Airport Branch', 'RGIA, Shamshabad', 'Hyderabad', 17.2403, 78.4294);

INSERT INTO customers (name, email, phone_number, licence_number)
VALUES
('Alice Kumar', 'alice@example.com', '9999999999', 'DL-1234'),
('Bob Rao', 'bob@example.com', '8888888888', 'DL-5678');

INSERT INTO categories (name, description)
VALUES
('SUV', 'Sports Utility Vehicles'),
('Sedan', 'Standard sedans');

INSERT INTO cars (vin, make, model, year, color, status, location_id)
VALUES
('VIN-SUV-0001', 'Toyota', 'Fortuner', 2022, 'White', 'available', 1),
('VIN-SED-0001', 'Honda', 'City', 2021, 'Black', 'available', 1),
('VIN-SUV-0002', 'Hyundai', 'Creta', 2023, 'Blue', 'available', 2);

INSERT INTO car_category (car_id, category_id, assigned_date, is_active)
VALUES
(1, 1, CURRENT_DATE, TRUE),
(2, 2, CURRENT_DATE, TRUE),
(3, 1, CURRENT_DATE, TRUE);

INSERT INTO reservations (customer_id, car_id, start_date, end_date, pickup_location_id, return_location_id, status, total_amount)
VALUES
(1, 1, '2025-08-01', '2025-08-05', 1, 1, 'reserved', 3500.00);
