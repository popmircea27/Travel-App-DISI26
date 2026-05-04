-- ============================================================
-- Flyway Migration: V3__add_search_indexes.sql
-- Description: Add indexes for frequently queried search/filter fields
-- Database: PostgreSQL
-- ============================================================

-- Search/filter by objective category.
-- Jira AC mapping: "location category field" -> current schema field objectives.category
CREATE INDEX IF NOT EXISTS idx_objectives_category_search
    ON objectives (category);

-- Search/filter by objective location/city text.
-- Jira AC mapping: "location city field" -> current schema field objectives.location_name
CREATE INDEX IF NOT EXISTS idx_objectives_location_name_search
    ON objectives (location_name);

-- Join/filter reviews by reviewed objective/location.
-- Jira AC mapping: "review location_id" -> current schema field reviews.objective_id
CREATE INDEX IF NOT EXISTS idx_reviews_objective_id_search
    ON reviews (objective_id);
