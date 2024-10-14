TRUNCATE TABLE transactions CASCADE;
TRUNCATE TABLE balances CASCADE;
TRUNCATE TABLE animals CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE adoption_requests CASCADE;

-- Insert balances
INSERT INTO balances (purpose, money_amount)
VALUES ('Medicine', 1000),
       ('Food', 500),
       ('General', 0);

-- Insert animals
INSERT INTO animals (name, type_of_animal, gender, is_castrated, health_status)
VALUES ('Buddy', 'Dog', 'MALE', true, 'HEALTHY'),
       ('Molly', 'Cat', 'FEMALE', false, 'SICK');

-- Insert users
INSERT INTO users (login, password, role, creation_date)
VALUES ('superuser', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'SUPERUSER', '2023-01-01'),
       ('customer', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'CUSTOMER', '2023-01-01'),
       ('emanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'EXPENSE_MANAGER', '2023-01-01'),
       ('amanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'ADOPTION_MANAGER', '2023-01-01');

-- Insert customers
INSERT INTO customers (gender, address, phone)
VALUES ('MALE', 'Moscow', '+79444333141'),
       ('MALE', 'Moscow', '+79444333111'),
       ('MALE', 'Moscow', '+79444333112'),
       ('MALE', 'Moscow', '+79444333131');

-- Adoption requests
WITH customer AS (SELECT id
                  FROM users
                  WHERE login = 'customer'),
     animal AS (SELECT id
                FROM animals
                WHERE name = 'Buddy')
INSERT
INTO adoption_requests (customer_id, animal_id, status, date_time)
VALUES ((SELECT id FROM customer), (SELECT id FROM animal), 'PENDING', '2023-01-01');

-- Donations
WITH customer AS (SELECT id
                 FROM users
                 WHERE login = 'customer'),
     balance AS (SELECT id
                    FROM balances
                    WHERE purpose = 'Medicine')
INSERT INTO transactions (date_time, user_id, balance_id, money_amount, is_donation)
VALUES ('2023-01-01', (SELECT id FROM customer), (SELECT id FROM balance), 100, true);