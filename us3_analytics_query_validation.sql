-- ============================================================
-- Sprint 3 US3 Validation Script
-- File: us3_analytics_query_validation.sql
-- Purpose: Validate optimized analytics queries and indexes
-- Database: PostgreSQL
-- ============================================================

-- Recommended:
-- Run these queries locally after Flyway migration V8.
-- Use EXPLAIN ANALYZE to confirm indexes are available and execution time is acceptable.

-- ============================================================
-- 1. Verify analytics indexes exist
-- ============================================================

SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
      'idx_analytics_visits_timestamp',
      'idx_analytics_visits_timestamp_objective',
      'idx_analytics_visits_timestamp_user',
      'idx_analytics_visits_visit_date',
      'idx_objectives_active_category',
      'idx_objectives_active_location_name',
      'idx_objectives_active_category_location',
      'idx_objectives_active_category_price',
      'idx_reviews_active_objective_rating',
      'idx_reviews_active_created_at',
      'idx_reviews_active_created_at_objective',
      'idx_wishlists_objective_user',
      'idx_wishlists_created_at',
      'idx_wishlists_created_at_objective',
      'idx_offers_active_valid_until_objective'
  )
ORDER BY tablename, indexname;

-- ============================================================
-- 2. Verify analytics views exist
-- ============================================================

SELECT
    table_schema,
    table_name
FROM information_schema.views
WHERE table_schema = 'public'
  AND table_name IN (
      'analytics_objective_summary',
      'analytics_daily_visits',
      'analytics_category_summary'
  )
ORDER BY table_name;

-- ============================================================
-- 3. Daily visits report
-- ============================================================

EXPLAIN ANALYZE
SELECT
    DATE(visit_timestamp) AS visit_date,
    COUNT(*) AS total_visits,
    COUNT(DISTINCT user_id) AS unique_users,
    COUNT(DISTINCT objective_id) AS unique_objectives
FROM analytics_visits
WHERE visit_timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(visit_timestamp)
ORDER BY visit_date DESC;

-- ============================================================
-- 4. Top visited objectives report
-- ============================================================

EXPLAIN ANALYZE
SELECT
    o.id,
    o.name,
    o.category,
    o.location_name,
    COUNT(av.id) AS visit_count
FROM objectives o
JOIN analytics_visits av
    ON av.objective_id = o.id
WHERE o.deleted_at IS NULL
  AND av.visit_timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY
    o.id,
    o.name,
    o.category,
    o.location_name
ORDER BY visit_count DESC
LIMIT 10;

-- ============================================================
-- 5. Objective performance report with reviews and wishlists
-- ============================================================

EXPLAIN ANALYZE
SELECT
    o.id,
    o.name,
    o.category,
    o.location_name,
    COUNT(DISTINCT av.id) AS visit_count,
    COUNT(DISTINCT r.id) AS review_count,
    ROUND(AVG(r.rating)::numeric, 2) AS average_rating,
    COUNT(DISTINCT w.user_id) AS wishlist_count
FROM objectives o
LEFT JOIN analytics_visits av
    ON av.objective_id = o.id
   AND av.visit_timestamp >= CURRENT_DATE - INTERVAL '30 days'
LEFT JOIN reviews r
    ON r.objective_id = o.id
   AND r.deleted_at IS NULL
LEFT JOIN wishlists w
    ON w.objective_id = o.id
WHERE o.deleted_at IS NULL
GROUP BY
    o.id,
    o.name,
    o.category,
    o.location_name
ORDER BY visit_count DESC, average_rating DESC NULLS LAST
LIMIT 10;

-- ============================================================
-- 6. Category report
-- ============================================================

EXPLAIN ANALYZE
SELECT
    category,
    objective_count,
    average_price,
    visit_count,
    review_count,
    average_rating
FROM analytics_category_summary
ORDER BY visit_count DESC, average_rating DESC NULLS LAST;

-- ============================================================
-- 7. Reusable objective summary view
-- ============================================================

EXPLAIN ANALYZE
SELECT
    objective_id,
    objective_name,
    category,
    location_name,
    visit_count,
    review_count,
    average_rating,
    wishlist_count,
    active_offer_count
FROM analytics_objective_summary
ORDER BY visit_count DESC, average_rating DESC NULLS LAST
LIMIT 10;

-- ============================================================
-- 8. Optional: inspect query plans manually
-- ============================================================

-- In the EXPLAIN ANALYZE output, check for:
-- - lower execution time compared with previous unindexed queries
-- - Index Scan / Bitmap Index Scan where dataset size makes index usage beneficial
-- - no repeated per-row application queries causing N+1 behavior
