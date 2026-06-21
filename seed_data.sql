-- =============================================================
-- RIDE HAILING SYSTEM — SEED DATA (ready to login immediately)
-- =============================================================
-- Passwords:
--   Riders  → Rider@123
--   Drivers → Driver@123
--
-- Run:
--   docker cp seed_data.sql postgres-docker-postgres-1:/seed_data.sql
--   docker exec -it postgres-docker-postgres-1 psql -U admin -d postgres -f /seed_data.sql
-- =============================================================

-- Create databases if they don't exist
SELECT 'CREATE DATABASE auth_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auth_db')\gexec
SELECT 'CREATE DATABASE user_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_db')\gexec
SELECT 'CREATE DATABASE ride_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ride_db')\gexec


-- =============================================================
-- AUTH DB
-- =============================================================
\c auth_db

-- Make sure the table exists (JPA creates it on startup, but just in case)
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    enabled       BOOLEAN      NOT NULL DEFAULT false,
    phone         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    role          VARCHAR(50)  NOT NULL,
    rating        DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    oauth_provider VARCHAR(255),
    oauth_id       VARCHAR(255)
);

-- Clear old seed data
DELETE FROM users WHERE email LIKE '%@example.com';

INSERT INTO users (id, name, email, enabled, phone, password_hash, role, rating, created_at) VALUES
  -- RIDERS  (password: Rider@123)
  ('a1000000-0000-0000-0000-000000000001', 'Ahmed Hassan',  'ahmed.rider@example.com',  true, '+20111234567', '$2a$10$Ou2JpYaPJ02txDrS6eCXe.hFqbBoY4X2U2awoGgJHN0qaenAEC1NS', 'RIDER', 5.0, NOW()),
  ('a1000000-0000-0000-0000-000000000002', 'Sara Ibrahim',  'sara.rider@example.com',   true, '+20122345678', '$2a$10$Ou2JpYaPJ02txDrS6eCXe.hFqbBoY4X2U2awoGgJHN0qaenAEC1NS', 'RIDER', 4.8, NOW()),
  ('a1000000-0000-0000-0000-000000000003', 'Omar Khaled',   'omar.rider@example.com',   true, '+20133456789', '$2a$10$Ou2JpYaPJ02txDrS6eCXe.hFqbBoY4X2U2awoGgJHN0qaenAEC1NS', 'RIDER', 4.9, NOW()),

  -- DRIVERS  (password: Driver@123)
  ('a2000000-0000-0000-0000-000000000001', 'Mohamed Ali',   'mohamed.driver@example.com', true, '+20109876543', '$2a$10$Jtzgbm9Bt3NU/kYSJEgJ0eOGKCkTaiVrphqvAsqTZZAEg2eC2Xx6y', 'DRIVER', 4.9, NOW()),
  ('a2000000-0000-0000-0000-000000000002', 'Karim Samir',   'karim.driver@example.com',  true, '+20198765432', '$2a$10$Jtzgbm9Bt3NU/kYSJEgJ0eOGKCkTaiVrphqvAsqTZZAEg2eC2Xx6y', 'DRIVER', 4.7, NOW()),
  ('a2000000-0000-0000-0000-000000000003', 'Youssef Adel',  'youssef.driver@example.com', true, '+20187654321', '$2a$10$Jtzgbm9Bt3NU/kYSJEgJ0eOGKCkTaiVrphqvAsqTZZAEg2eC2Xx6y', 'DRIVER', 4.8, NOW());


-- =============================================================
-- USER DB
-- =============================================================
\c user_db

CREATE TABLE IF NOT EXISTS driver_profiles (
    id                  UUID PRIMARY KEY,
    user_id             UUID         NOT NULL UNIQUE,
    license_number      VARCHAR(255) NOT NULL UNIQUE,
    status              VARCHAR(50)  NOT NULL DEFAULT 'OFFLINE',
    current_lat         DOUBLE PRECISION,
    current_lng         DOUBLE PRECISION,
    location_updated_at TIMESTAMP,
    rating              DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    total_ratings       INTEGER      NOT NULL DEFAULT 0,
    total_rides         INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vehicles (
    id                UUID PRIMARY KEY,
    driver_profile_id UUID         NOT NULL UNIQUE,
    plate_number      VARCHAR(20)  NOT NULL UNIQUE,
    make              VARCHAR(50)  NOT NULL,
    model             VARCHAR(50)  NOT NULL,
    color             VARCHAR(30)  NOT NULL,
    year              INTEGER,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS driver_ratings (
    id           UUID PRIMARY KEY,
    driver_id    UUID    NOT NULL,
    passenger_id UUID    NOT NULL,
    ride_id      UUID    NOT NULL UNIQUE,
    rating       INTEGER NOT NULL,
    comment      VARCHAR(500),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

DELETE FROM driver_ratings;
DELETE FROM vehicles;
DELETE FROM driver_profiles;

INSERT INTO driver_profiles (id, user_id, license_number, status, current_lat, current_lng, location_updated_at, rating, total_ratings, total_rides, created_at) VALUES
  ('b1000000-0000-0000-0000-000000000001', 'a2000000-0000-0000-0000-000000000001', 'EG-LIC-2020-1001', 'AVAILABLE', 30.0444, 31.2357, NOW(), 4.9, 120, 115, NOW()),
  ('b1000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000002', 'EG-LIC-2019-2002', 'AVAILABLE', 30.0600, 31.2200, NOW(), 4.7,  85,  80, NOW()),
  ('b1000000-0000-0000-0000-000000000003', 'a2000000-0000-0000-0000-000000000003', 'EG-LIC-2021-3003', 'OFFLINE',   30.0750, 31.2900, NOW(), 4.8,  60,  58, NOW());

INSERT INTO vehicles (id, driver_profile_id, plate_number, make, model, color, year, created_at) VALUES
  ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'ABC-1234', 'Toyota',  'Corolla', 'White',  2021, NOW()),
  ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'DEF-5678', 'Hyundai', 'Elantra', 'Silver', 2020, NOW()),
  ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003', 'GHI-9012', 'Kia',     'Cerato',  'Black',  2022, NOW());

INSERT INTO driver_ratings (id, driver_id, passenger_id, ride_id, rating, comment, created_at) VALUES
  ('d1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 5, 'Excellent driver, very polite!',       NOW() - INTERVAL '2 days'),
  ('d1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 4, 'Good ride, a bit slow.',               NOW() - INTERVAL '1 day'),
  ('d1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000003', 5, 'Car was clean, driver was on time.',   NOW() - INTERVAL '3 days');


-- =============================================================
-- RIDE DB
-- =============================================================
\c ride_db

CREATE TABLE IF NOT EXISTS rides (
    id           UUID PRIMARY KEY,
    rider_id     UUID         NOT NULL,
    driver_id    UUID,
    status       VARCHAR(50)  NOT NULL DEFAULT 'REQUESTED',
    pickup_lat   DOUBLE PRECISION NOT NULL,
    pickup_lng   DOUBLE PRECISION NOT NULL,
    dropoff_lat  DOUBLE PRECISION NOT NULL,
    dropoff_lng  DOUBLE PRECISION NOT NULL,
    requested_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    accepted_at  TIMESTAMP,
    started_at   TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fares (
    id               UUID PRIMARY KEY,
    ride_id          UUID           NOT NULL UNIQUE,
    base_fare        NUMERIC(10,2)  NOT NULL,
    distance_km      NUMERIC(8,3)   NOT NULL,
    surge_multiplier NUMERIC(4,2)   NOT NULL DEFAULT 1.0,
    total_amount     NUMERIC(10,2)  NOT NULL,
    currency         VARCHAR(10)    NOT NULL DEFAULT 'EGP',
    calculated_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ride_cancellations (
    id           UUID PRIMARY KEY,
    ride_id      UUID         NOT NULL UNIQUE,
    cancelled_by VARCHAR(50)  NOT NULL,
    reason       VARCHAR(255),
    cancelled_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

DELETE FROM ride_cancellations;
DELETE FROM fares;
DELETE FROM rides;

INSERT INTO rides (id, rider_id, driver_id, status, pickup_lat, pickup_lng, dropoff_lat, dropoff_lng, requested_at, accepted_at, started_at, completed_at) VALUES
  -- Completed
  ('e1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'COMPLETED', 30.0444, 31.2357, 30.0626, 31.2497, NOW()-INTERVAL '2 days', NOW()-INTERVAL '2 days'+INTERVAL '3 min', NOW()-INTERVAL '2 days'+INTERVAL '7 min', NOW()-INTERVAL '2 days'+INTERVAL '28 min'),
  ('e1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'COMPLETED', 30.0500, 31.2400, 30.0750, 31.2750, NOW()-INTERVAL '1 day',  NOW()-INTERVAL '1 day' +INTERVAL '4 min', NOW()-INTERVAL '1 day' +INTERVAL '8 min', NOW()-INTERVAL '1 day' +INTERVAL '30 min'),
  ('e1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000001', 'COMPLETED', 30.0300, 31.2200, 30.0600, 31.2600, NOW()-INTERVAL '3 days', NOW()-INTERVAL '3 days'+INTERVAL '2 min', NOW()-INTERVAL '3 days'+INTERVAL '5 min', NOW()-INTERVAL '3 days'+INTERVAL '20 min'),

  -- Cancelled
  ('e1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', NULL, 'CANCELLED', 30.0500, 31.2450, 30.0700, 31.2700, NOW()-INTERVAL '6 hours', NULL, NULL, NULL),

  -- Active (IN_PROGRESS) — driver 2 is currently on a ride
  ('e1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'IN_PROGRESS', 30.0300, 31.2100, 30.0700, 31.2500, NOW()-INTERVAL '15 min', NOW()-INTERVAL '12 min', NOW()-INTERVAL '8 min', NULL),

  -- Pending (REQUESTED) — no driver yet
  ('e1000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000003', NULL, 'REQUESTED', 30.0450, 31.2380, 30.0600, 31.2600, NOW()-INTERVAL '1 min', NULL, NULL, NULL);

INSERT INTO fares (id, ride_id, base_fare, distance_km, surge_multiplier, total_amount, currency, calculated_at) VALUES
  ('f1000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 10.00, 3.200, 1.00, 26.40, 'EGP', NOW()-INTERVAL '2 days'+INTERVAL '28 min'),
  ('f1000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 10.00, 4.800, 1.20, 46.08, 'EGP', NOW()-INTERVAL '1 day' +INTERVAL '30 min'),
  ('f1000000-0000-0000-0000-000000000003', 'e1000000-0000-0000-0000-000000000003', 10.00, 3.900, 1.00, 31.20, 'EGP', NOW()-INTERVAL '3 days'+INTERVAL '20 min');

INSERT INTO ride_cancellations (id, ride_id, cancelled_by, reason, cancelled_at) VALUES
  ('f9000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000004', 'RIDER', 'Change of plans', NOW()-INTERVAL '5 hours 55 min');

