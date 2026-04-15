-- ============================================================
-- Flyway Migration: V1__init.sql
-- Description: Initial database schema for TravelPoints app
-- Database: Azure DB for PostgreSQL
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TYPE user_role AS ENUM ('ADMIN', 'TOURIST');

CREATE TABLE users (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           user_role    NOT NULL DEFAULT 'TOURIST',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. OBJECTIVES
--    FK: admin_id -> users(id)
-- ============================================================
CREATE TABLE objectives (
    id             UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    audio_url      VARCHAR(255),
    category       VARCHAR(100)   NOT NULL,
    price          DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    location_name  VARCHAR(255)   NOT NULL,
    admin_id       UUID           NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_objectives_admin
        FOREIGN KEY (admin_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_objectives_category ON objectives(category);
CREATE INDEX idx_objectives_location ON objectives(location_name);
CREATE INDEX idx_objectives_admin    ON objectives(admin_id);

-- ============================================================
-- 3. REVIEWS
--    FK: objective_id -> objectives(id)
--    FK: user_id -> users(id)
-- ============================================================
CREATE TABLE reviews (
    id             UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    objective_id   UUID      NOT NULL,
    user_id        UUID      NOT NULL,
    rating         INT       NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment        TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_objective
        FOREIGN KEY (objective_id) REFERENCES objectives(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_reviews_objective ON reviews(objective_id);
CREATE INDEX idx_reviews_user      ON reviews(user_id);

-- ============================================================
-- 4. OFFERS
--    FK: objective_id -> objectives(id)
-- ============================================================
CREATE TABLE offers (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    objective_id   UUID         NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    valid_until    TIMESTAMP,

    CONSTRAINT fk_offers_objective
        FOREIGN KEY (objective_id) REFERENCES objectives(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_offers_objective   ON offers(objective_id);
CREATE INDEX idx_offers_valid_until ON offers(valid_until);

-- ============================================================
-- 5. WISHLISTS (composite PK)
--    FK: user_id -> users(id)
--    FK: objective_id -> objectives(id)
-- ============================================================
CREATE TABLE wishlists (
    user_id        UUID      NOT NULL,
    objective_id   UUID      NOT NULL,
    added_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, objective_id),

    CONSTRAINT fk_wishlists_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wishlists_objective
        FOREIGN KEY (objective_id) REFERENCES objectives(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 6. ANALYTICS_VISITS
--    FK: objective_id -> objectives(id)
--    FK: user_id -> users(id)
-- ============================================================
CREATE TABLE analytics_visits (
    id               UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    objective_id     UUID      NOT NULL,
    user_id          UUID      NOT NULL,
    visit_timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_visits_objective
        FOREIGN KEY (objective_id) REFERENCES objectives(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_visits_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_visits_objective  ON analytics_visits(objective_id);
CREATE INDEX idx_visits_user       ON analytics_visits(user_id);
CREATE INDEX idx_visits_timestamp  ON analytics_visits(visit_timestamp);
