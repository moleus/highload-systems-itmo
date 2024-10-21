TRUNCATE TABLE transaction CASCADE;
TRUNCATE TABLE balance CASCADE;
TRUNCATE TABLE animal CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE customer CASCADE;
TRUNCATE TABLE adoption_request CASCADE;

-- Insert balance
INSERT INTO balance (id, purpose, money_amount)
VALUES (-1, 'Medicine', 1000),
       (-2, 'Food', 500),
       (-3, 'General', 0);

-- Insert animals
INSERT INTO animal (id, name, type_of_animal, gender, is_castrated, health_status)
VALUES (-1, 'Buddy', 'Dog', 'MALE', true, 'HEALTHY'),
       (-2, 'Molly', 'Cat', 'FEMALE', false, 'SICK');

-- Insert users
-- password is '1234'
INSERT INTO users (id, login, password, role, creation_date)
VALUES (-1, 'superuser', '$2a$10$ISK8t2WKiPOJ64D8uHTk0OAKHneW3fg/1kxfw0UHbtkJ7GyKH6g2m', 'SUPERUSER', '2023-01-01'),
       (-2, 'customer', '$2a$10$ISK8t2WKiPOJ64D8uHTk0OAKHneW3fg/1kxfw0UHbtkJ7GyKH6g2m', 'CUSTOMER', '2023-01-01'),
       (-3, 'emanager', '$2a$10$ISK8t2WKiPOJ64D8uHTk0OAKHneW3fg/1kxfw0UHbtkJ7GyKH6g2m', 'EXPENSE_MANAGER', '2023-01-01'),
       (-4, 'amanager', '$2a$10$ISK8t2WKiPOJ64D8uHTk0OAKHneW3fg/1kxfw0UHbtkJ7GyKH6g2m', 'ADOPTION_MANAGER', '2023-01-01');

-- Insert customer
INSERT INTO customer (id, gender, address, phone)
VALUES (-1, 'MALE', 'Moscow', '+79444333141'),
       (-2, 'MALE', 'Moscow', '+79444333111'),
       (-3, 'MALE', 'Moscow', '+79444333112'),
       (-4, 'MALE', 'Moscow', '+79444333131');

-- Adoption request
WITH customer AS (SELECT id
                  FROM users
                  WHERE login = 'customer'),
     animal AS (SELECT id
                FROM animal
                WHERE name = 'Buddy')
INSERT
INTO adoption_request (id, customer_id, animal_id, status, date_time)
VALUES (-1, (SELECT id FROM customer), (SELECT id FROM animal), 'PENDING', '2023-01-01');

-- Donations
WITH customer AS (SELECT id
                 FROM users
                 WHERE login = 'customer'),
     balance AS (SELECT id
                    FROM balance
                    WHERE purpose = 'Medicine')
INSERT INTO transaction (date_time, user_id, balance_id, money_amount, is_donation)
VALUES ('2023-01-01', (SELECT id FROM customer), (SELECT id FROM balance), 100, true);
