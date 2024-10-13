TRUNCATE TABLE transactions CASCADE;
TRUNCATE TABLE balances CASCADE;
TRUNCATE TABLE animals CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE adoption_requests CASCADE;

-- Insert balances
INSERT INTO balances (id, purpose, money_amount)
VALUES (-1, 'Medicine', 1000),
       (-2, 'Food', 500),
       (-3, 'General', 0);

-- Insert animals
INSERT INTO animals (id, name, type_of_animal, gender, is_castrated, health_status)
VALUES (-1, 'Buddy', 'Dog', 'MALE', true, 'HEALTHY'),
       (-2, 'Molly', 'Cat', 'FEMALE', false, 'SICK');

-- Insert users
INSERT INTO users (id, login, password, role, creation_date)
VALUES (-1, 'superuser', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'SUPERUSER', '2023-01-01'),
       (-2, 'customer', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'CUSTOMER', '2023-01-01'),
       (-3, 'emanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'EXPENSE_MANAGER', '2023-01-01'),
       (-4, 'amanager', '$2a$10$vQDZDHzuXMXRMDqilDvQxu.8lPD2YyeI9Se5aDgog/roV1whaWWgG', 'ADOPTION_MANAGER', '2023-01-01');

-- Insert customers
INSERT INTO customers (id, gender, address, phone)
VALUES (-1, 'MALE', 'Moscow', '+79444333141'),
       (-2, 'MALE', 'Moscow', '+79444333111'),
       (-3, 'MALE', 'Moscow', '+79444333112'),
       (-4, 'MALE', 'Moscow', '+79444333131');

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
