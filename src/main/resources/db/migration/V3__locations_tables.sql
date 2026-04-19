-- ============================================================
-- Flyway Migration: V3__locations_tables.sql
-- Description: Add locations and location_reviews tables for Travel App REST API
-- Note: Rename from "reviews" to "location_reviews" to avoid conflict with existing objectives reviews
-- ============================================================

-- ============================================================
-- 1. LOCATIONS (independent from objectives)
-- ============================================================
CREATE TABLE locations (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    country          VARCHAR(100),
    city             VARCHAR(100),
    image_url        TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_locations_city ON locations(city);
CREATE INDEX idx_locations_country ON locations(country);

-- ============================================================
-- 2. LOCATION_REVIEWS (for locations, separate from objectives reviews)
--    FK: location_id -> locations(id)
--    FK: user_id -> users(id)
-- ============================================================
CREATE TABLE location_reviews (
    id               UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    location_id      UUID      NOT NULL,
    user_id          UUID      NOT NULL,
    rating           INT       NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment          TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location_reviews_location
        FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_location_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_location_reviews_location ON location_reviews(location_id);
CREATE INDEX idx_location_reviews_user ON location_reviews(user_id);

