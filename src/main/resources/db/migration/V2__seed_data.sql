-- ============================================================
-- Flyway Migration: V2__seed_data.sql
-- Description: Insert sample/test data for TravelPoints app
-- ============================================================

-- ============================================================
-- 1. USERS (1 admin + 3 tourists)
-- ============================================================
-- Passwords are bcrypt hashes of 'password123'
INSERT INTO users (id, email, password_hash, role, created_at) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin@travelpoints.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',   '2025-01-01 10:00:00'),
    ('b0000000-0000-0000-0000-000000000001', 'john.doe@example.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TOURIST', '2025-01-15 12:00:00'),
    ('b0000000-0000-0000-0000-000000000002', 'jane.smith@example.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TOURIST', '2025-02-01 09:30:00'),
    ('b0000000-0000-0000-0000-000000000003', 'alex.tourist@example.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TOURIST', '2025-03-10 14:00:00');

-- ============================================================
-- 2. OBJECTIVES (10 sample tourist objectives)
-- ============================================================
INSERT INTO objectives (id, name, description, audio_url, category, price, location_name, admin_id, created_at) VALUES
    ('c0000000-0000-0000-0000-000000000001',
     'Bran Castle',
     'Known as Dracula''s Castle, Bran Castle is a national monument and landmark in Transylvania. Built in the 14th century, it sits on the border between Transylvania and Wallachia.',
     'https://storage.travelpoints.com/audio/bran-castle.mp3',
     'Castle', 50.00, 'Bran, Brașov', 'a0000000-0000-0000-0000-000000000001', '2025-01-05 11:00:00'),

    ('c0000000-0000-0000-0000-000000000002',
     'Peleș Castle',
     'A Neo-Renaissance castle nestled in the Carpathian Mountains. It was the summer residence of Romanian kings and is considered one of the most beautiful castles in Europe.',
     'https://storage.travelpoints.com/audio/peles-castle.mp3',
     'Castle', 70.00, 'Sinaia, Prahova', 'a0000000-0000-0000-0000-000000000001', '2025-01-06 09:00:00'),

    ('c0000000-0000-0000-0000-000000000003',
     'Salina Turda',
     'An ancient salt mine in Turda, Transylvania, converted into a unique underground amusement park and museum with a subterranean lake.',
     'https://storage.travelpoints.com/audio/salina-turda.mp3',
     'Museum', 40.00, 'Turda, Cluj', 'a0000000-0000-0000-0000-000000000001', '2025-01-10 10:00:00'),

    ('c0000000-0000-0000-0000-000000000004',
     'Palace of the Parliament',
     'The world''s heaviest building and the second-largest administrative building after the Pentagon. A monumental structure in the heart of Bucharest.',
     'https://storage.travelpoints.com/audio/parliament.mp3',
     'Architecture', 60.00, 'Bucharest', 'a0000000-0000-0000-0000-000000000001', '2025-01-12 13:00:00'),

    ('c0000000-0000-0000-0000-000000000005',
     'Transfăgărășan Highway',
     'A spectacular mountain road crossing the southern section of the Carpathian Mountains. Often called the best road in the world by Top Gear.',
     NULL,
     'Nature', 0.00, 'Argeș / Sibiu', 'a0000000-0000-0000-0000-000000000001', '2025-02-01 08:00:00'),

    ('c0000000-0000-0000-0000-000000000006',
     'Sighișoara Citadel',
     'A beautifully preserved medieval citadel and UNESCO World Heritage Site. The birthplace of Vlad the Impaler.',
     'https://storage.travelpoints.com/audio/sighisoara.mp3',
     'Citadel', 25.00, 'Sighișoara, Mureș', 'a0000000-0000-0000-0000-000000000001', '2025-02-05 10:30:00'),

    ('c0000000-0000-0000-0000-000000000007',
     'Painted Monasteries of Bucovina',
     'A group of Romanian Orthodox monasteries in southern Bucovina, famous for their vivid exterior frescoes. UNESCO World Heritage Sites.',
     'https://storage.travelpoints.com/audio/bucovina.mp3',
     'Religious', 15.00, 'Suceava', 'a0000000-0000-0000-0000-000000000001', '2025-02-10 11:00:00'),

    ('c0000000-0000-0000-0000-000000000008',
     'Danube Delta',
     'Europe''s best preserved delta and a UNESCO Biosphere Reservation. Home to over 300 species of birds and 160 species of fish.',
     'https://storage.travelpoints.com/audio/danube-delta.mp3',
     'Nature', 30.00, 'Tulcea', 'a0000000-0000-0000-0000-000000000001', '2025-02-15 09:00:00'),

    ('c0000000-0000-0000-0000-000000000009',
     'Cluj-Napoca Botanical Garden',
     'One of the largest botanical gardens in Europe, featuring over 10,000 plant species across themed gardens including a Japanese garden.',
     NULL,
     'Park', 10.00, 'Cluj-Napoca, Cluj', 'a0000000-0000-0000-0000-000000000001', '2025-03-01 08:30:00'),

    ('c0000000-0000-0000-0000-000000000010',
     'Corvin Castle',
     'One of the largest castles in Europe, built in Gothic-Renaissance style. Located in Hunedoara, it is one of the Seven Wonders of Romania.',
     'https://storage.travelpoints.com/audio/corvin-castle.mp3',
     'Castle', 45.00, 'Hunedoara', 'a0000000-0000-0000-0000-000000000001', '2025-03-05 14:00:00');

-- ============================================================
-- 3. REVIEWS
-- ============================================================
INSERT INTO reviews (id, objective_id, user_id, rating, comment, created_at) VALUES
    ('d0000000-0000-0000-0000-000000000001',
     'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
     5, 'Absolutely stunning! The atmosphere is incredible, especially at sunset. A must-visit in Transylvania.',
     '2025-02-20 15:30:00'),

    ('d0000000-0000-0000-0000-000000000002',
     'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002',
     4, 'Great historical site. It was a bit crowded but worth the visit. The audio guide was very helpful.',
     '2025-03-01 10:00:00'),

    ('d0000000-0000-0000-0000-000000000003',
     'c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001',
     5, 'The most beautiful castle I have ever seen. The interior is breathtaking with all the original furniture.',
     '2025-03-05 11:00:00'),

    ('d0000000-0000-0000-0000-000000000004',
     'c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002',
     5, 'An absolutely unique experience! The underground lake and amusement park are unlike anything else.',
     '2025-03-10 14:00:00'),

    ('d0000000-0000-0000-0000-000000000005',
     'c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003',
     4, 'Very impressive salt mine. Gets busy on weekends so I recommend visiting on a weekday.',
     '2025-03-12 16:00:00'),

    ('d0000000-0000-0000-0000-000000000006',
     'c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001',
     5, 'The views are out of this world! Best road trip experience. Make sure to go during summer months.',
     '2025-03-15 09:00:00'),

    ('d0000000-0000-0000-0000-000000000007',
     'c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000003',
     4, 'An amazing nature reserve. We saw pelicans and many other birds. Boat tours are highly recommended.',
     '2025-03-20 12:00:00'),

    ('d0000000-0000-0000-0000-000000000008',
     'c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000002',
     5, 'Corvin Castle is magnificent. The Gothic architecture is incredibly well preserved. Highly recommend!',
     '2025-04-01 10:30:00');

-- ============================================================
-- 4. OFFERS
-- ============================================================
INSERT INTO offers (id, objective_id, title, description, valid_until) VALUES
    ('e0000000-0000-0000-0000-000000000001',
     'c0000000-0000-0000-0000-000000000001',
     'Summer Family Package',
     'Buy 2 adult tickets, get 1 child ticket free. Valid for families visiting Bran Castle during summer.',
     '2025-08-31 23:59:59'),

    ('e0000000-0000-0000-0000-000000000002',
     'c0000000-0000-0000-0000-000000000002',
     'Early Bird Discount',
     '20% off entry tickets for visits before 10 AM. Start your day early at Peleș Castle!',
     '2025-12-31 23:59:59'),

    ('e0000000-0000-0000-0000-000000000003',
     'c0000000-0000-0000-0000-000000000003',
     'Student Discount',
     '50% off for students with valid student ID. Explore the underground wonders of Salina Turda.',
     '2025-09-30 23:59:59'),

    ('e0000000-0000-0000-0000-000000000004',
     'c0000000-0000-0000-0000-000000000008',
     'Spring Boat Tour Special',
     'Book a 3-day Danube Delta boat tour and get 15% off. Includes guide and meals.',
     '2025-06-30 23:59:59');

-- ============================================================
-- 5. WISHLISTS
-- ============================================================
INSERT INTO wishlists (user_id, objective_id, added_at) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', '2025-02-01 10:00:00'),
    ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000005', '2025-02-05 14:00:00'),
    ('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000008', '2025-03-01 09:00:00'),
    ('b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', '2025-02-10 11:00:00'),
    ('b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000006', '2025-02-15 16:00:00'),
    ('b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000010', '2025-03-05 08:00:00'),
    ('b0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', '2025-03-10 10:00:00'),
    ('b0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000007', '2025-03-12 12:00:00');

-- ============================================================
-- 6. ANALYTICS_VISITS
--    Spread across different hours and months for analytics
-- ============================================================
INSERT INTO analytics_visits (id, objective_id, user_id, visit_timestamp) VALUES
    -- January visits
    ('f0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', '2025-01-20 09:30:00'),
    ('f0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', '2025-01-22 14:00:00'),
    -- February visits
    ('f0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', '2025-02-10 10:00:00'),
    ('f0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', '2025-02-15 11:30:00'),
    ('f0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000003', '2025-02-20 16:00:00'),
    -- March visits
    ('f0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', '2025-03-05 08:00:00'),
    ('f0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001', '2025-03-10 13:00:00'),
    ('f0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000003', '2025-03-15 10:00:00'),
    ('f0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000002', '2025-03-20 15:00:00'),
    -- April visits
    ('f0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', '2025-04-01 09:00:00'),
    ('f0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', '2025-04-05 11:00:00'),
    ('f0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000003', '2025-04-10 17:00:00'),
    ('f0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000001', '2025-04-12 12:00:00'),
    ('f0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', '2025-04-15 14:30:00'),
    ('f0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000003', '2025-04-20 10:00:00');
