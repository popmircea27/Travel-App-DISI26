-- ============================================================
-- Flyway Migration: V8__optimize_analytics_queries.sql
-- Description: Optimize analytics/reporting queries for Sprint 3 US3
-- Database: PostgreSQL
-- ============================================================

-- Goal:
-- - Support fast report generation
-- - Improve aggregation queries
-- - Add indexes used by analytics filters, joins, and GROUP BY queries
-- - Provide reusable analytics views for common reports

-- ============================================================
-- 1. Analytics visit indexes
-- ============================================================

-- Report visits within a date range, usually ordered/grouped by timestamp.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_timestamp
    ON analytics_visits (visit_timestamp DESC);

-- Report visits per objective within a date range.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_timestamp_objective
    ON analytics_visits (visit_timestamp DESC, objective_id);

-- Report visits per user within a date range.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_timestamp_user
    ON analytics_visits (visit_timestamp DESC, user_id);

-- Efficient daily aggregation.
-- DATE(visit_timestamp) is commonly used for charts and reports.
CREATE INDEX IF NOT EXISTS idx_analytics_visits_visit_date
    ON analytics_visits ((DATE(visit_timestamp)));

-- ============================================================
-- 2. Objective/reporting indexes
-- ============================================================

-- Category/location reports should ignore soft-deleted objectives.
CREATE INDEX IF NOT EXISTS idx_objectives_active_category
    ON objectives (category)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_objectives_active_location_name
    ON objectives (location_name)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_objectives_active_category_location
    ON objectives (category, location_name)
    WHERE deleted_at IS NULL;

-- Price statistics by category/location.
CREATE INDEX IF NOT EXISTS idx_objectives_active_category_price
    ON objectives (category, price)
    WHERE deleted_at IS NULL;

-- ============================================================
-- 3. Review/reporting indexes
-- ============================================================

-- Average rating per objective and rating distribution.
CREATE INDEX IF NOT EXISTS idx_reviews_active_objective_rating
    ON reviews (objective_id, rating)
    WHERE deleted_at IS NULL;

-- Review volume and average rating over time.
CREATE INDEX IF NOT EXISTS idx_reviews_active_created_at
    ON reviews (created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_active_created_at_objective
    ON reviews (created_at DESC, objective_id)
    WHERE deleted_at IS NULL;

-- ============================================================
-- 4. Wishlist/reporting indexes
-- ============================================================

-- Count wishlist saves per objective.
CREATE INDEX IF NOT EXISTS idx_wishlists_objective_user
    ON wishlists (objective_id, user_id);

-- Count wishlist activity by creation date after Sprint 3 US2 timestamps.
CREATE INDEX IF NOT EXISTS idx_wishlists_created_at
    ON wishlists (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_wishlists_created_at_objective
    ON wishlists (created_at DESC, objective_id);

-- ============================================================
-- 5. Offer/reporting indexes
-- ============================================================

-- Count active/non-expired offers per objective.
CREATE INDEX IF NOT EXISTS idx_offers_active_valid_until_objective
    ON offers (valid_until, objective_id)
    WHERE deleted_at IS NULL;

-- ============================================================
-- 6. Reusable analytics views
-- ============================================================

-- Objective-level metrics used by dashboards/reports.
CREATE OR REPLACE VIEW analytics_objective_summary AS
SELECT
    o.id AS objective_id,
    o.name AS objective_name,
    o.category,
    o.location_name,
    o.price,
    COUNT(DISTINCT av.id) AS visit_count,
    COUNT(DISTINCT r.id) AS review_count,
    ROUND(AVG(r.rating)::numeric, 2) AS average_rating,
    COUNT(DISTINCT w.user_id) AS wishlist_count,
    COUNT(DISTINCT off.id) FILTER (
        WHERE off.deleted_at IS NULL
          AND (off.valid_until IS NULL OR off.valid_until >= CURRENT_DATE)
    ) AS active_offer_count
FROM objectives o
LEFT JOIN analytics_visits av
    ON av.objective_id = o.id
LEFT JOIN reviews r
    ON r.objective_id = o.id
   AND r.deleted_at IS NULL
LEFT JOIN wishlists w
    ON w.objective_id = o.id
LEFT JOIN offers off
    ON off.objective_id = o.id
   AND off.deleted_at IS NULL
WHERE o.deleted_at IS NULL
GROUP BY
    o.id,
    o.name,
    o.category,
    o.location_name,
    o.price;

-- Daily visit metrics used by charts.
CREATE OR REPLACE VIEW analytics_daily_visits AS
SELECT
    DATE(visit_timestamp) AS visit_date,
    COUNT(*) AS total_visits,
    COUNT(DISTINCT objective_id) AS unique_objectives_visited,
    COUNT(DISTINCT user_id) AS unique_users
FROM analytics_visits
GROUP BY DATE(visit_timestamp);

-- Category-level objective statistics.
CREATE OR REPLACE VIEW analytics_category_summary AS
SELECT
    o.category,
    COUNT(DISTINCT o.id) AS objective_count,
    ROUND(AVG(o.price)::numeric, 2) AS average_price,
    COUNT(DISTINCT av.id) AS visit_count,
    COUNT(DISTINCT r.id) AS review_count,
    ROUND(AVG(r.rating)::numeric, 2) AS average_rating
FROM objectives o
LEFT JOIN analytics_visits av
    ON av.objective_id = o.id
LEFT JOIN reviews r
    ON r.objective_id = o.id
   AND r.deleted_at IS NULL
WHERE o.deleted_at IS NULL
GROUP BY o.category;
