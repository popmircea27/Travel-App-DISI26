-- Run before and after applying V3 to compare plans/timing.
-- In psql, enable timing with: \timing on

EXPLAIN ANALYZE
SELECT *
FROM objectives
WHERE category = 'Castle';

EXPLAIN ANALYZE
SELECT *
FROM objectives
WHERE location_name = 'Bucharest';

EXPLAIN ANALYZE
SELECT r.*
FROM reviews r
WHERE r.objective_id = 'c0000000-0000-0000-0000-000000000001';

EXPLAIN ANALYZE
SELECT o.id, o.name, o.category, o.location_name, AVG(r.rating) AS avg_rating
FROM objectives o
LEFT JOIN reviews r ON r.objective_id = o.id
WHERE o.category = 'Castle'
  AND o.location_name = 'Bran, Brașov'
GROUP BY o.id, o.name, o.category, o.location_name;

-- Verify indexes exist:
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('objectives', 'reviews')
  AND indexname IN (
      'idx_objectives_category_search',
      'idx_objectives_location_name_search',
      'idx_reviews_objective_id_search'
  )
ORDER BY tablename, indexname;
