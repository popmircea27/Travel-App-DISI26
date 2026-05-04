-- ============================================================
-- US3 local constraint validation script
-- Purpose: prove invalid data is rejected by PostgreSQL constraints
-- Run after Flyway applies V5__enforce_database_constraints.sql
-- ============================================================

-- 1. List constraints and unique indexes added/used for data integrity
SELECT
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type
FROM information_schema.table_constraints tc
WHERE tc.table_schema = 'public'
  AND tc.table_name IN ('users', 'objectives', 'reviews', 'offers', 'wishlists', 'analytics_visits')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;

SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
      'uq_users_email_lower',
      'uq_objectives_admin_name_location',
      'uq_offers_objective_title'
  )
ORDER BY tablename, indexname;

-- ============================================================
-- 2. Negative tests
-- Each block should print a NOTICE confirming rejection.
-- ============================================================

DO $$
BEGIN
    BEGIN
        INSERT INTO objectives (name, category, price, location_name, admin_id)
        VALUES ('Invalid Negative Price', 'Museum', -10.00, 'Bucharest', 'a0000000-0000-0000-0000-000000000001');
        RAISE EXCEPTION 'FAILED: negative objective price was accepted';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: negative objective price rejected';
    END;

    BEGIN
        INSERT INTO objectives (name, category, price, location_name, admin_id)
        VALUES ('   ', 'Museum', 10.00, 'Bucharest', 'a0000000-0000-0000-0000-000000000001');
        RAISE EXCEPTION 'FAILED: blank objective name was accepted';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: blank objective name rejected';
    END;

    BEGIN
        INSERT INTO objectives (name, category, price, location_name, admin_id)
        VALUES ('Invalid FK Objective', 'Museum', 10.00, 'Bucharest', 'ffffffff-ffff-ffff-ffff-ffffffffffff');
        RAISE EXCEPTION 'FAILED: invalid objective admin_id was accepted';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASSED: invalid objective admin_id rejected';
    END;

    BEGIN
        INSERT INTO reviews (objective_id, user_id, rating, comment)
        VALUES ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 6, 'Invalid rating');
        RAISE EXCEPTION 'FAILED: invalid review rating was accepted';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: invalid review rating rejected';
    END;

    BEGIN
        INSERT INTO reviews (objective_id, user_id, rating, comment)
        VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'b0000000-0000-0000-0000-000000000001', 5, 'Invalid objective FK');
        RAISE EXCEPTION 'FAILED: invalid review objective_id was accepted';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASSED: invalid review objective_id rejected';
    END;

    BEGIN
        INSERT INTO reviews (objective_id, user_id, rating, comment)
        VALUES ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 5, 'Duplicate review');
        RAISE EXCEPTION 'FAILED: duplicate review was accepted';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: duplicate review rejected';
    END;

    BEGIN
        INSERT INTO offers (objective_id, title, description, valid_until)
        VALUES ('c0000000-0000-0000-0000-000000000001', '   ', 'Invalid blank title', CURRENT_TIMESTAMP + INTERVAL '30 days');
        RAISE EXCEPTION 'FAILED: blank offer title was accepted';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: blank offer title rejected';
    END;

    BEGIN
        INSERT INTO wishlists (user_id, objective_id)
        VALUES ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002');
        RAISE EXCEPTION 'FAILED: duplicate wishlist row was accepted';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: duplicate wishlist row rejected';
    END;
END $$;

-- ============================================================
-- 3. Positive sanity test: valid data should still be accepted.
-- The transaction is rolled back so the database remains clean.
-- ============================================================

BEGIN;

INSERT INTO objectives (id, name, description, category, price, location_name, admin_id)
VALUES (
    'c9999999-9999-9999-9999-999999999999',
    'Valid Test Objective',
    'Temporary row used only for US3 validation.',
    'Museum',
    20.00,
    'Test City',
    'a0000000-0000-0000-0000-000000000001'
);

INSERT INTO reviews (objective_id, user_id, rating, comment)
VALUES (
    'c9999999-9999-9999-9999-999999999999',
    'b0000000-0000-0000-0000-000000000003',
    5,
    'Valid review test.'
);

SELECT 'PASSED: valid objective and review accepted' AS result;

ROLLBACK;
