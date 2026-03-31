-- V3__seed_test_products.sql
-- Inserts 20 additional products across all three categories (Electronics, Food, Cleaning)
-- to support pagination testing. No stock or inventory movements are created.

INSERT INTO products (name, description, sku, unit_id, purchase_price, sale_price, category_id, supplier_id, active, created_at, updated_at) VALUES

    -- Electronics (category_id = 1, unit_id = 1 UND, supplier_id = 1)
    ('Teclado Mecánico Redragon',    'Teclado mecánico RGB switch blue',                 'TECH-003', 1, 150000.00, 210000.00, 1, 1, true, NOW(), NOW()),
    ('Monitor Samsung 24"',          'Monitor Full HD 75Hz panel IPS',                   'TECH-004', 1, 780000.00, 980000.00, 1, 1, true, NOW(), NOW()),
    ('Audífonos Sony WH-1000XM4',    'Audífonos inalámbricos con cancelación de ruido',  'TECH-005', 1, 900000.00, 1200000.00, 1, 1, true, NOW(), NOW()),
    ('Disco Duro Externo 1TB',        'Disco USB 3.0 portátil',                           'TECH-006', 1, 220000.00, 310000.00, 1, 1, true, NOW(), NOW()),
    ('Webcam Logitech C920',          'Cámara web Full HD 1080p con micrófono',           'TECH-007', 1, 340000.00, 470000.00, 1, 1, true, NOW(), NOW()),
    ('Hub USB-C 7 en 1',              'Adaptador multipuerto HDMI, USB-A, SD, RJ45',      'TECH-008', 1,  95000.00, 140000.00, 1, 1, true, NOW(), NOW()),
    ('Tablet Samsung Galaxy A8',      'Tablet 10.5 pulgadas, 64GB, WiFi',                'TECH-009', 1, 850000.00, 1100000.00, 1, 1, true, NOW(), NOW()),

    -- Food (category_id = 2, unit_id = 2 KG o 3 LT, supplier_id = 2)
    ('Azúcar Manuelita 5kg',          'Azúcar blanca refinada en bolsa',                 'ALIM-003', 2,  14000.00,  20000.00, 2, 2, true, NOW(), NOW()),
    ('Leche Entera Alquería 1L',      'Leche entera UHT larga vida',                     'ALIM-004', 3,   4000.00,   6000.00, 2, 2, true, NOW(), NOW()),
    ('Harina de Trigo 1kg',           'Harina todo uso tamizada',                        'ALIM-005', 2,   5500.00,   8500.00, 2, 2, true, NOW(), NOW()),
    ('Pasta Spaghetti 500g',          'Pasta de sémola de trigo durum',                  'ALIM-006', 2,   4200.00,   6500.00, 2, 2, true, NOW(), NOW()),
    ('Sal Marina 1kg',                'Sal marina fina sin yodo',                        'ALIM-007', 2,   2500.00,   4000.00, 2, 2, true, NOW(), NOW()),
    ('Café Molido Águila 500g',       'Café molido de origen colombiano',                'ALIM-008', 2,  18000.00,  27000.00, 2, 2, true, NOW(), NOW()),
    ('Atún en Lata Van Camps 170g',   'Atún en agua con sal',                            'ALIM-009', 1,   4800.00,   7200.00, 2, 2, true, NOW(), NOW()),

    -- Cleaning (category_id = 3, unit_id = 3 LT o 1 UND, supplier_id = 3)
    ('Blanqueador Concentrado 1L',    'Hipoclorito de sodio al 5.25%',                   'LIMP-003', 3,   6000.00,  10000.00, 3, 3, true, NOW(), NOW()),
    ('Limpiapisos Fabuloso 1L',       'Limpiador multiusos con fragancia lavanda',       'LIMP-004', 3,   9500.00,  15000.00, 3, 3, true, NOW(), NOW()),
    ('Esponja Scotch-Brite x3',       'Esponjas de doble cara para platos',              'LIMP-005', 1,   5800.00,   9000.00, 3, 3, true, NOW(), NOW()),
    ('Detergente en Polvo 2kg',       'Detergente para ropa con suavizante',             'LIMP-006', 2,  22000.00,  32000.00, 3, 3, true, NOW(), NOW()),
    ('Limpiavidrios Cristalín 750ml', 'Limpiador para vidrios y superficies brillantes', 'LIMP-007', 3,   7500.00,  12000.00, 3, 3, true, NOW(), NOW()),
    ('Guantes de Látex Talla M x2',   'Guantes reutilizables para limpieza del hogar',  'LIMP-008', 1,   8000.00,  13500.00, 3, 3, true, NOW(), NOW());
