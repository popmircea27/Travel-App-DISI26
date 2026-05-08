-- ============================================================
-- Flyway Migration: V3__locations_tables.sql
-- Description: Add locations and location_reviews tables for Travel App REST API
-- Note: Rename from "reviews" to "location_reviews" to avoid conflict with existing objectives reviews
-- ============================================================

-- ============================================================
-- 1. LOCATIONS (independent from objectives)
-- ============================================================
CREATE TABLE locations (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    country          VARCHAR(100),
    city             VARCHAR(100),
    image_url        TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_locations_city ON locations(city);
CREATE INDEX idx_locations_country ON locations(country);

-- ============================================================
-- 2. LOCATION_REVIEWS (for locations, separate from objectives reviews)
--    FK: location_id -> locations(id)
--    FK: user_id -> users(id)
-- ============================================================
CREATE TABLE location_reviews (
    id               UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    location_id      UUID      NOT NULL,
    user_id          UUID      NOT NULL,
    rating           INT       NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment          TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location_reviews_location
        FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_location_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_location_reviews_location ON location_reviews(location_id);
CREATE INDEX idx_location_reviews_user ON location_reviews(user_id);

-- ============================================================
-- 3. LOCATIONS (seed data - corresponding to objectives)
-- ============================================================
INSERT INTO locations (id, name, description, latitude, longitude, country, city, image_url, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000001',
     'Bran Castle',
     'Known as Dracula''s Castle, Bran Castle is a national monument and landmark in Transylvania. Built in the 14th century, it sits on the border between Transylvania and Wallachia.',
     45.5148, 25.3836,
     'Romania', 'Bran, Brașov',
     'https://images.unsplash.com/photo-1582719509272-11c3b6ecda79?w=800&q=80',
     '2025-01-05 11:00:00', '2025-01-05 11:00:00'),

    ('f0000000-0000-0000-0000-000000000002',
     'Peleș Castle',
     'A Neo-Renaissance castle nestled in the Carpathian Mountains. It was the summer residence of Romanian kings and is considered one of the most beautiful castles in Europe.',
     45.3414, 25.5349,
     'Romania', 'Sinaia, Prahova',
     'https://images.unsplash.com/photo-1568513776144-792051bfd88f?w=800&q=80',
     '2025-01-06 09:00:00', '2025-01-06 09:00:00'),

    ('f0000000-0000-0000-0000-000000000003',
     'Salina Turda',
     'An ancient salt mine in Turda, Transylvania, converted into a unique underground amusement park and museum with a subterranean lake.',
     46.5742, 23.7761,
     'Romania', 'Turda, Cluj',
     'https://images.unsplash.com/photo-1587134634028-41d8f41112c5?w=800&q=80',
     '2025-01-10 10:00:00', '2025-01-10 10:00:00'),

    ('f0000000-0000-0000-0000-000000000004',
     'Palace of the Parliament',
     'The world''s heaviest building and the second-largest administrative building after the Pentagon. A monumental structure in the heart of Bucharest.',
     44.4268, 26.0881,
     'Romania', 'Bucharest',
     'https://images.unsplash.com/photo-1583881694315-b8ef92603f88?w=800&q=80',
     '2025-01-12 13:00:00', '2025-01-12 13:00:00'),

    ('f0000000-0000-0000-0000-000000000005',
     'Transfăgărășan Highway',
     'A spectacular mountain road crossing the southern section of the Carpathian Mountains. Often called the best road in the world by Top Gear.',
     45.3667, 24.6667,
     'Romania', 'Argeș / Sibiu',
     'https://images.unsplash.com/photo-1531343413893-3dd90bb584d4?w=800&q=80',
     '2025-02-01 08:00:00', '2025-02-01 08:00:00'),

    ('f0000000-0000-0000-0000-000000000006',
     'Sighișoara Citadel',
     'A beautifully preserved medieval citadel and UNESCO World Heritage Site. The birthplace of Vlad the Impaler.',
     46.2197, 24.7956,
     'Romania', 'Sighișoara, Mureș',
     'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800&q=80',
     '2025-02-05 10:30:00', '2025-02-05 10:30:00'),

    ('f0000000-0000-0000-0000-000000000007',
     'Painted Monasteries of Bucovina',
     'A group of Romanian Orthodox monasteries in southern Bucovina, famous for their vivid exterior frescoes. UNESCO World Heritage Sites.',
     47.6364, 26.2597,
     'Romania', 'Suceava',
     'https://images.unsplash.com/photo-1577720643272-265f434e9c7f?w=800&q=80',
     '2025-02-10 11:00:00', '2025-02-10 11:00:00'),

    ('f0000000-0000-0000-0000-000000000008',
     'Danube Delta',
     'Europe''s best preserved delta and a UNESCO Biosphere Reservation. Home to over 300 species of birds and 160 species of fish.',
     44.8071, 28.8638,
     'Romania', 'Tulcea',
     'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&q=80',
     '2025-02-15 09:00:00', '2025-02-15 09:00:00'),

    ('f0000000-0000-0000-0000-000000000009',
     'Cluj-Napoca Botanical Garden',
     'One of the largest botanical gardens in Europe, featuring over 10,000 plant species across themed gardens including a Japanese garden.',
     46.7712, 23.6236,
     'Romania', 'Cluj-Napoca, Cluj',
     'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80',
     '2025-03-01 08:30:00', '2025-03-01 08:30:00'),

    ('f0000000-0000-0000-0000-000000000010',
     'Corvin Castle',
     'One of the largest castles in Europe, built in Gothic-Renaissance style. Located in Hunedoara, it is one of the Seven Wonders of Romania.',
     45.7667, 22.9167,
     'Romania', 'Hunedoara',
     'https://images.unsplash.com/photo-1534762114416-67f6f5f3aef7?w=800&q=80',
     '2025-03-05 14:00:00', '2025-03-05 14:00:00');

