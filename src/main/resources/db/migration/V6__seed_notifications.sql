-- ============================================================
-- Flyway Migration: V6__seed_notifications.sql
-- Description: Insert sample data for notifications
-- ============================================================
INSERT INTO notifications (id, user_id, title, message, is_read, created_at) VALUES
    ('80000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Welcome!', 'Welcome to TravelPoints! Explore our best locations.', true, '2025-01-20 10:00:00'),
    ('80000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'New Offer', 'Check out the new Summer Family Package for Bran Castle.', false, '2025-06-01 10:00:00'),
    ('80000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'New Offer', 'Early Bird Discount is now active for Peleș Castle!', false, '2025-06-05 12:00:00');