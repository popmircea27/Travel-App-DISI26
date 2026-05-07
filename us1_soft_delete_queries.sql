-- ============================================================
-- Sprint 3 US1: Soft delete query examples and validation
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. Soft delete operations - use UPDATE instead of DELETE
-- ============================================================

-- Soft delete an objective.
-- Replace :objective_id with the real id from the application.
-- UPDATE objectives
-- SET deleted_at = CURRENT_TIMESTAMP
-- WHERE id = :objective_id
--   AND deleted_at IS NULL;

-- Soft delete a review.
-- UPDATE reviews
-- SET deleted_at = CURRENT_TIMESTAMP
-- WHERE id = :review_id
--   AND deleted_at IS NULL;

-- Soft delete an offer.
-- UPDATE offers
-- SET deleted_at = CURRENT_TIMESTAMP
-- WHERE id = :offer_id
--   AND deleted_at IS NULL;

-- Soft delete a user.
-- Usually this should be used carefully because users are referenced by reviews/objectives.
-- UPDATE users
-- SET deleted_at = CURRENT_TIMESTAMP
-- WHERE id = :user_id
--   AND deleted_at IS NULL;

-- ============================================================
-- 2. Updated read queries - always filter deleted records
-- ============================================================

-- Search/filter active objectives.
EXPLAIN ANALYZE
SELECT id, name, category, location_name, price, created_at
FROM objectives
WHERE deleted_at IS NULL
  AND category = 'Museum'
  AND location_name = 'London'
ORDER BY created_at DESC;

-- Fetch active objective details with active reviews and active offers.
EXPLAIN ANALYZE
SELECT
    o.id,
    o.name,
    o.category,
    o.location_name,
    o.price,
    COALESCE(r.review_count, 0) AS review_count,
    COALESCE(r.average_rating, 0) AS average_rating,
    COALESCE(ofr.active_offer_count, 0) AS active_offer_count
FROM objectives o
LEFT JOIN (
    SELECT
        objective_id,
        COUNT(*) AS review_count,
        AVG(rating) AS average_rating
    FROM reviews
    WHERE deleted_at IS NULL
    GROUP BY objective_id
) r ON r.objective_id = o.id
LEFT JOIN (
    SELECT
        objective_id,
        COUNT(*) AS active_offer_count
    FROM offers
    WHERE deleted_at IS NULL
      AND (valid_until IS NULL OR valid_until >= CURRENT_DATE)
    GROUP BY objective_id
) ofr ON ofr.objective_id = o.id
WHERE o.deleted_at IS NULL
ORDER BY o.created_at DESC;

-- Same idea using the active views created by the migration.
EXPLAIN ANALYZE
SELECT id, name, category, location_name, price
FROM active_objectives
WHERE category = 'Museum'
ORDER BY created_at DESC;

-- ============================================================
-- 3. Local validation - proves soft-deleted records are hidden
-- ============================================================

BEGIN;

-- Pick one objective from seed data.
WITH picked_objective AS (
    SELECT id
    FROM objectives
    WHERE deleted_at IS NULL
    ORDER BY id
    LIMIT 1
), soft_deleted AS (
    UPDATE objectives o
    SET deleted_at = CURRENT_TIMESTAMP
    FROM picked_objective p
    WHERE o.id = p.id
    RETURNING o.id
)
SELECT
    sd.id AS soft_deleted_objective_id,
    EXISTS (
        SELECT 1
        FROM objectives o
        WHERE o.id = sd.id
    ) AS still_exists_in_table,
    EXISTS (
        SELECT 1
        FROM active_objectives ao
        WHERE ao.id = sd.id
    ) AS visible_in_active_view
FROM soft_deleted sd;

-- Expected result:
-- still_exists_in_table = true
-- visible_in_active_view = false

ROLLBACK;

-- ============================================================
-- 4. Verify migration objects
-- ============================================================

SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name IN ('users', 'objectives', 'reviews', 'offers')
  AND column_name = 'deleted_at'
ORDER BY table_name;

SELECT indexname, indexdef
FROM pg_indexes
WHERE indexname LIKE '%active%'
ORDER BY indexname;

SELECT table_name
FROM information_schema.views
WHERE table_name IN ('active_users', 'active_objectives', 'active_reviews', 'active_offers')
ORDER BY table_name;
