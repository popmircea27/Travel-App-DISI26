-- ============================================================
-- US2 verification and optimized query examples
-- ============================================================

-- 1. Verify foreign keys are present.
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column,
    rc.delete_rule
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
   AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
   AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints rc
    ON rc.constraint_name = tc.constraint_name
   AND rc.constraint_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- 2. Verify US2 indexes exist.
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
      'idx_wishlists_objective_id',
      'idx_reviews_objective_id_created_at',
      'idx_reviews_user_id_created_at',
      'idx_offers_objective_id_valid_until',
      'idx_analytics_visits_objective_id_timestamp',
      'idx_analytics_visits_user_id_timestamp',
      'idx_objectives_category_location_price',
      'idx_objectives_created_at'
  )
ORDER BY indexname;

-- 3. Optimized objective list query.
-- Avoids N+1 by fetching review summary, active offers count and wishlist status in one query.
EXPLAIN ANALYZE
SELECT
    o.id,
    o.name,
    o.category,
    o.location_name,
    o.price,
    COALESCE(rs.review_count, 0) AS review_count,
    COALESCE(rs.average_rating, 0) AS average_rating,
    COALESCE(os.active_offer_count, 0) AS active_offer_count,
    CASE WHEN w.objective_id IS NULL THEN false ELSE true END AS in_wishlist
FROM objectives o
LEFT JOIN (
    SELECT
        objective_id,
        COUNT(*) AS review_count,
        ROUND(AVG(rating)::numeric, 2) AS average_rating
    FROM reviews
    GROUP BY objective_id
) rs ON rs.objective_id = o.id
LEFT JOIN (
    SELECT objective_id, COUNT(*) AS active_offer_count
    FROM offers
    WHERE valid_until IS NULL OR valid_until >= CURRENT_TIMESTAMP
    GROUP BY objective_id
) os ON os.objective_id = o.id
LEFT JOIN wishlists w
    ON w.objective_id = o.id
   AND w.user_id = 'b0000000-0000-0000-0000-000000000001'
WHERE o.category = 'Castle'
  AND o.location_name ILIKE '%Brașov%'
ORDER BY o.created_at DESC
LIMIT 20;

-- 4. Optimized objective details query.
-- Fetches objective + admin + aggregated review information in one request.
EXPLAIN ANALYZE
SELECT
    o.id,
    o.name,
    o.description,
    o.category,
    o.location_name,
    o.price,
    u.email AS admin_email,
    COUNT(r.id) AS review_count,
    ROUND(AVG(r.rating)::numeric, 2) AS average_rating
FROM objectives o
JOIN users u ON u.id = o.admin_id
LEFT JOIN reviews r ON r.objective_id = o.id
WHERE o.id = 'c0000000-0000-0000-0000-000000000001'
GROUP BY o.id, u.email;

-- 5. Optimized reviews query for an objective.
-- Uses idx_reviews_objective_id_created_at.
EXPLAIN ANALYZE
SELECT
    r.id,
    r.rating,
    r.comment,
    r.created_at,
    u.email AS reviewer_email
FROM reviews r
JOIN users u ON u.id = r.user_id
WHERE r.objective_id = 'c0000000-0000-0000-0000-000000000001'
ORDER BY r.created_at DESC
LIMIT 50;

-- 6. Optimized analytics query.
-- Uses idx_analytics_visits_objective_id_timestamp.
EXPLAIN ANALYZE
SELECT
    av.objective_id,
    COUNT(*) AS visit_count
FROM analytics_visits av
WHERE av.objective_id = 'c0000000-0000-0000-0000-000000000001'
  AND av.visit_timestamp >= TIMESTAMP '2025-01-01'
  AND av.visit_timestamp <  TIMESTAMP '2025-05-01'
GROUP BY av.objective_id;
