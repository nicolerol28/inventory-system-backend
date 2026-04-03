-- V6__seed_inactive_users_categories_units.sql
-- Inserts inactive records for users, categories, and units to support
-- filtering and display of inactive entities in the frontend.

-- ============================================================
-- Inactive users (role = OPERATOR, active = false)
-- Password hash corresponds to 'Demo1234!' (bcrypt, cost 12)
-- ============================================================
INSERT INTO users (name, email, password, role, active, created_at, updated_at) VALUES
    (
        'Valentina Ospina',
        'vospina@inventory.com',
        '$2a$12$RxgHyl0IDfS8OyAboCZyr.Keh8b6BNtiGdmms9y7dqr.92m1Jv4gm',
        'OPERATOR',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'Andrés Felipe Morales',
        'afmorales@inventory.com',
        '$2a$12$RxgHyl0IDfS8OyAboCZyr.Keh8b6BNtiGdmms9y7dqr.92m1Jv4gm',
        'OPERATOR',
        FALSE,
        NOW(),
        NOW()
    );

-- ============================================================
-- Inactive categories (active = false)
-- ============================================================
INSERT INTO categories (name, active, created_at) VALUES
    ('Papelería', FALSE, NOW()),
    ('Ferretería', FALSE, NOW());

-- ============================================================
-- Inactive units (active = false)
-- ============================================================
INSERT INTO units (name, symbol, active, created_at) VALUES
    ('Metro',     'MT',  FALSE, NOW()),
    ('Mililitro', 'ML',  FALSE, NOW());
