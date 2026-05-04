-- ============================================================
-- Flyway Migration: V5__enforce_database_constraints.sql
-- Description: Enforce database constraints for Sprint 2 US3
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- V1 already defines the main NOT NULL, primary key, foreign key,
-- and rating CHECK constraints. This migration strengthens integrity
-- with business-level constraints that prevent invalid values and duplicates.

-- ============================================================
-- 1. USERS integrity
-- ============================================================

ALTER TABLE users
    ADD CONSTRAINT chk_users_email_not_blank
    CHECK (length(trim(email)) > 0);

ALTER TABLE users
    ADD CONSTRAINT chk_users_password_hash_not_blank
    CHECK (length(trim(password_hash)) > 0);

-- Email is already UNIQUE in V1. This unique index makes the rule
-- case-insensitive, so Admin@Test.com and admin@test.com cannot both exist.
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_lower
    ON users (lower(email));

-- ============================================================
-- 2. OBJECTIVES integrity
-- ============================================================

ALTER TABLE objectives
    ADD CONSTRAINT chk_objectives_name_not_blank
    CHECK (length(trim(name)) > 0);

ALTER TABLE objectives
    ADD CONSTRAINT chk_objectives_category_not_blank
    CHECK (length(trim(category)) > 0);

ALTER TABLE objectives
    ADD CONSTRAINT chk_objectives_location_name_not_blank
    CHECK (length(trim(location_name)) > 0);

ALTER TABLE objectives
    ADD CONSTRAINT chk_objectives_price_non_negative
    CHECK (price >= 0);

-- Prevent the same admin from creating duplicate objectives with the same name
-- in the same location.
CREATE UNIQUE INDEX IF NOT EXISTS uq_objectives_admin_name_location
    ON objectives (admin_id, lower(name), lower(location_name));

-- ============================================================
-- 3. REVIEWS integrity
-- ============================================================

-- rating CHECK and foreign keys already exist in V1.
-- This prevents one user from reviewing the same objective multiple times.
ALTER TABLE reviews
    ADD CONSTRAINT uq_reviews_user_objective
    UNIQUE (user_id, objective_id);

-- ============================================================
-- 4. OFFERS integrity
-- ============================================================

ALTER TABLE offers
    ADD CONSTRAINT chk_offers_title_not_blank
    CHECK (length(trim(title)) > 0);

-- Optional text can be NULL, but if present it should not be only whitespace.
ALTER TABLE offers
    ADD CONSTRAINT chk_offers_description_not_blank_when_present
    CHECK (description IS NULL OR length(trim(description)) > 0);

-- Prevent duplicate offer titles for the same objective.
CREATE UNIQUE INDEX IF NOT EXISTS uq_offers_objective_title
    ON offers (objective_id, lower(title));

-- ============================================================
-- 5. WISHLISTS integrity
-- ============================================================

-- V1 already enforces:
-- - NOT NULL user_id/objective_id/added_at
-- - primary key (user_id, objective_id)
-- - foreign keys to users/objectives

-- ============================================================
-- 6. ANALYTICS_VISITS integrity
-- ============================================================

-- V1 already enforces:
-- - NOT NULL objective_id/user_id/visit_timestamp
-- - foreign keys to users/objectives
