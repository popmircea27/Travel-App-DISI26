-- ============================================================
-- Flyway Migration: V4__optimize_relationship_queries.sql
-- Description: Optimize relationship-based queries and joins
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- Existing foreign keys are already correctly defined in V1:
--   objectives.admin_id        -> users.id
--   reviews.objective_id       -> objectives.id
--   reviews.user_id            -> users.id
--   offers.objective_id        -> objectives.id
--   wishlists.user_id          -> users.id
--   wishlists.objective_id     -> objectives.id
--   analytics_visits.objective_id -> objectives.id
--   analytics_visits.user_id      -> users.id
--
-- This migration adds missing relationship-side indexes and composite indexes
-- for frequent fetch/filter patterns, reducing join cost and helping avoid
-- N+1-style repeated lookups at the SQL level.

-- Wishlist queries are usually made in both directions:
-- 1) all objectives saved by a user: covered by PRIMARY KEY (user_id, objective_id)
-- 2) all users who saved an objective / joining from objectives to wishlists: needs objective_id index
CREATE INDEX IF NOT EXISTS idx_wishlists_objective_id
    ON wishlists (objective_id);

-- Fetch reviews for an objective ordered by latest first.
CREATE INDEX IF NOT EXISTS idx_reviews_objective_id_created_at
    ON reviews (objective_id, created_at DESC);

-- Fetch reviews written by a user ordered by latest first.
CREATE INDEX IF NOT EXISTS idx_reviews_user_id_created_at
    ON reviews (user_id, created_at DESC);

-- Fetch active offers for an objective.
CREATE INDEX IF NOT EXISTS idx_offers_objective_id_valid_until
    ON offers (objective_id, valid_until);

-- Fetch analytics visits for an objective within a date range.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_objective_id_timestamp
    ON analytics_visits (objective_id, visit_timestamp DESC);

-- Fetch analytics visits for a user within a date range.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_user_id_timestamp
    ON analytics_visits (user_id, visit_timestamp DESC);

-- Common objective browsing pattern: category + location + price.
CREATE INDEX IF NOT EXISTS idx_objectives_category_location_price
    ON objectives (category, location_name, price);

-- Common objective listing pattern by newest first.
CREATE INDEX IF NOT EXISTS idx_objectives_created_at
    ON objectives (created_at DESC);
