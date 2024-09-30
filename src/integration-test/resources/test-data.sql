-- Insert balances
INSERT INTO balances (purpose, money_amount) VALUES ('Medicine', 1000);
INSERT INTO balances (purpose, money_amount) VALUES ('Food', 500);
INSERT INTO balances (purpose, money_amount) VALUES ('General', 0);

-- Insert animals
INSERT INTO animals (name, type_of_animal, gender, is_castrated, health_status) VALUES ('Buddy', 'Dog', 'MALE', true, 'HEALTHY');
INSERT INTO animals (name, type_of_animal, gender, is_castrated, health_status) VALUES ('Molly', 'Cat', 'FEMALE', false, 'SICK');

-- Insert users
INSERT INTO users (login, password, role, creation_date) VALUES ('superuser', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'SUPERUSER', '2023-01-01');
INSERT INTO users (login, password, role, creation_date) VALUES ('customer', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'CUSTOMER', '2023-01-01');
INSERT INTO users (login, password, role, creation_date) VALUES ('emanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'EXPENSE_MANAGER', '2023-01-01');
INSERT INTO users (login, password, role, creation_date) VALUES ('amanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'ADOPTION_MANAGER', '2023-01-01');

-- Insert customers
INSERT INTO customers (gender, address, phone) VALUES ('MALE', 'Moscow', '+79444333141');
INSERT INTO customers (gender, address, phone) VALUES ('MALE', 'Moscow', '+79444333111');
INSERT INTO customers (gender, address, phone) VALUES ('MALE', 'Moscow', '+79444333112');
INSERT INTO customers (gender, address, phone) VALUES ('MALE', 'Moscow', '+79444333131');

-- Adoption requests
INSERT INTO adoption_requests (customer_id, animal_id, status, date_time) VALUES (2, 1, 'PENDING', '2023-01-01');
