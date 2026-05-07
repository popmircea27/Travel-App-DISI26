-- ============================================================
-- Flyway Migration: V7__add_audit_timestamps.sql
-- Description: Add automatic created_at and updated_at timestamp tracking
-- Sprint 3 US2
-- Database: PostgreSQL
-- ============================================================

-- Goal:
-- - created_at stores when a row was created
-- - updated_at stores when a row was last modified
-- - updated_at is automatically refreshed by triggers on UPDATE

-- ============================================================
-- 1. Add timestamp columns to entity tables
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE objectives
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE reviews
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE offers
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE wishlists
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- analytics_visits already has visit_timestamp for event tracking.
-- updated_at is still useful if analytics records are corrected later.
ALTER TABLE analytics_visits
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- ============================================================
-- 2. Backfill existing rows where needed
-- ============================================================

UPDATE users
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE objectives
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE reviews
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE offers
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE wishlists
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE analytics_visits
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

-- ============================================================
-- 3. Create reusable updated_at trigger function
-- ============================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 4. Attach triggers to update updated_at automatically
-- ============================================================

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;
CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_objectives_set_updated_at ON objectives;
CREATE TRIGGER trg_objectives_set_updated_at
BEFORE UPDATE ON objectives
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_reviews_set_updated_at ON reviews;
CREATE TRIGGER trg_reviews_set_updated_at
BEFORE UPDATE ON reviews
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_offers_set_updated_at ON offers;
CREATE TRIGGER trg_offers_set_updated_at
BEFORE UPDATE ON offers
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_wishlists_set_updated_at ON wishlists;
CREATE TRIGGER trg_wishlists_set_updated_at
BEFORE UPDATE ON wishlists
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_analytics_visits_set_updated_at ON analytics_visits;
CREATE TRIGGER trg_analytics_visits_set_updated_at
BEFORE UPDATE ON analytics_visits
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 5. Add indexes useful for auditing/recent-change queries
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_users_updated_at
    ON users (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_objectives_updated_at
    ON objectives (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_reviews_updated_at
    ON reviews (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_offers_updated_at
    ON offers (updated_at DESC);
