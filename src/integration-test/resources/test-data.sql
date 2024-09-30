-- Insert balances
INSERT INTO balances (id, purpose, money_amount) VALUES (1, 'Medicine', 1000);
INSERT INTO balances (id, purpose, money_amount) VALUES (2, 'Food', 500);
INSERT INTO balances (id, purpose, money_amount) VALUES (3, 'General', 0);

-- Insert animals
INSERT INTO animals (id, name, type_of_animal, gender, is_castrated, health_status) VALUES (1, 'Buddy', 'Dog', 'MALE', true, 'HEALTHY');
INSERT INTO animals (id, name, type_of_animal, gender, is_castrated, health_status) VALUES (2, 'Molly', 'Cat', 'FEMALE', false, 'SICK');

-- Insert users
INSERT INTO users (id, login, password, role, creation_date) VALUES (1, 'superuser', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'SUPERUSER', '2023-01-01');
INSERT INTO users (id, login, password, role, creation_date) VALUES (2, 'customer', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'CUSTOMER', '2023-01-01');
INSERT INTO users (id, login, password, role, creation_date) VALUES (3, 'emanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'EXPENSE_MANAGER', '2023-01-01');
INSERT INTO users (id, login, password, role, creation_date) VALUES (4, 'amanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'ADOPTION_MANAGER', '2023-01-01');

-- Insert customers
INSERT INTO customers (id, gender, address, phone) VALUES (1, 'MALE', 'Moscow', '+79444333141');
INSERT INTO customers (id, gender, address, phone) VALUES (2, 'MALE', 'Moscow', '+79444333111');
INSERT INTO customers (id, gender, address, phone) VALUES (3, 'MALE', 'Moscow', '+79444333112');
INSERT INTO customers (id, gender, address, phone) VALUES (4, 'MALE', 'Moscow', '+79444333131');

-- Adoption requests
INSERT INTO adoption_requests (id, customer_id, animal_id, status, date_time) VALUES (1, 2, 1, 'PENDING', '2023-01-01');
INSERT INTO adoption_requests (customer_id, animal_id, status, date_time) VALUES (2, 1, 'PENDING', '2023-01-01');
