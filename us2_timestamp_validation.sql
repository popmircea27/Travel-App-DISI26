-- ============================================================
-- Sprint 3 US2 Validation Script
-- File: us2_timestamp_validation.sql
-- Purpose: Verify created_at and updated_at are populated and updated automatically
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. Confirm timestamp columns exist
-- ============================================================

SELECT
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN (
      'users',
      'objectives',
      'reviews',
      'offers',
      'wishlists',
      'analytics_visits'
  )
  AND column_name IN ('created_at', 'updated_at')
ORDER BY table_name, column_name;

-- ============================================================
-- 2. Confirm update triggers exist
-- ============================================================

SELECT
    event_object_table AS table_name,
    trigger_name,
    action_timing,
    event_manipulation
FROM information_schema.triggers
WHERE trigger_schema = 'public'
  AND trigger_name LIKE 'trg_%_set_updated_at'
ORDER BY event_object_table, trigger_name;

-- ============================================================
-- 3. Verify timestamps are populated on existing data
-- ============================================================

SELECT
    'users' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(created_at) AS rows_with_created_at,
    COUNT(updated_at) AS rows_with_updated_at
FROM users
UNION ALL
SELECT
    'objectives',
    COUNT(*),
    COUNT(created_at),
    COUNT(updated_at)
FROM objectives
UNION ALL
SELECT
    'reviews',
    COUNT(*),
    COUNT(created_at),
    COUNT(updated_at)
FROM reviews
UNION ALL
SELECT
    'offers',
    COUNT(*),
    COUNT(created_at),
    COUNT(updated_at)
FROM offers
UNION ALL
SELECT
    'wishlists',
    COUNT(*),
    COUNT(created_at),
    COUNT(updated_at)
FROM wishlists
UNION ALL
SELECT
    'analytics_visits',
    COUNT(*),
    COUNT(created_at),
    COUNT(updated_at)
FROM analytics_visits;

-- ============================================================
-- 4. Verify updated_at changes automatically on UPDATE
-- ============================================================

-- This test uses objectives because objectives are frequently updated entities.
-- It updates one existing objective using a no-op data change.
-- If your local database has no objectives, seed the database first using V2.

BEGIN;

SELECT
    id,
    name,
    created_at,
    updated_at AS before_updated_at
FROM objectives
ORDER BY id
LIMIT 1;

-- Give PostgreSQL a visible timestamp difference for local manual testing.
SELECT pg_sleep(1);

UPDATE objectives
SET name = name
WHERE id = (
    SELECT id
    FROM objectives
    ORDER BY id
    LIMIT 1
);

SELECT
    id,
    name,
    created_at,
    updated_at AS after_updated_at
FROM objectives
ORDER BY id
LIMIT 1;

-- Expected:
-- - created_at remains unchanged
-- - updated_at becomes newer after the UPDATE

ROLLBACK;

-- ============================================================
-- 5. Example queries for latest created/updated records
-- ============================================================

SELECT
    id,
    name,
    created_at,
    updated_at
FROM objectives
WHERE deleted_at IS NULL
ORDER BY updated_at DESC
LIMIT 10;

SELECT
    id,
    objective_id,
    user_id,
    rating,
    created_at,
    updated_at
FROM reviews
WHERE deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 10;
