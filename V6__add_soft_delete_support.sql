-- ============================================================
-- Flyway Migration: V6__add_soft_delete_support.sql
-- Description: Add soft delete support for Sprint 3 US1
-- Database: PostgreSQL
-- ============================================================

-- Soft delete strategy:
-- - deleted_at IS NULL  => active record
-- - deleted_at IS NOT NULL => deleted record
--
-- Application DELETE operations should be replaced with UPDATE statements:
--   UPDATE <table> SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id;
--
-- Normal read queries should filter with:
--   WHERE deleted_at IS NULL

-- ============================================================
-- 1. Add deleted_at columns to main entity tables
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE objectives
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE offers
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- ============================================================
-- 2. Add partial indexes for active-record queries
-- ============================================================

-- Active users by id/email.
CREATE INDEX IF NOT EXISTS idx_users_active_id
    ON users (id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_active_email_lower
    ON users (lower(email))
    WHERE deleted_at IS NULL;

-- Active objectives used by search/filter queries.
CREATE INDEX IF NOT EXISTS idx_objectives_active_id
    ON objectives (id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_objectives_active_category_location_price
    ON objectives (category, location_name, price)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_objectives_active_admin_id
    ON objectives (admin_id)
    WHERE deleted_at IS NULL;

-- Active reviews used when fetching objective details.
CREATE INDEX IF NOT EXISTS idx_reviews_active_objective_created_at
    ON reviews (objective_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_active_user_created_at
    ON reviews (user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- Active offers used when fetching objective details.
CREATE INDEX IF NOT EXISTS idx_offers_active_objective_valid_until
    ON offers (objective_id, valid_until)
    WHERE deleted_at IS NULL;

-- ============================================================
-- 3. Add active-record views to standardize query filtering
-- ============================================================

CREATE OR REPLACE VIEW active_users AS
SELECT *
FROM users
WHERE deleted_at IS NULL;

CREATE OR REPLACE VIEW active_objectives AS
SELECT *
FROM objectives
WHERE deleted_at IS NULL;

CREATE OR REPLACE VIEW active_reviews AS
SELECT *
FROM reviews
WHERE deleted_at IS NULL;

CREATE OR REPLACE VIEW active_offers AS
SELECT *
FROM offers
WHERE deleted_at IS NULL;
