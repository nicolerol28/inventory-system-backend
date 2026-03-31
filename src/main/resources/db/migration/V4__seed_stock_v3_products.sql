-- V4__seed_stock_v3_products.sql
-- Creates stock records for the 20 products inserted in V3 (IDs 7-26) across all
-- three warehouses (IDs 1-3), and registers the corresponding PURCHASE_ENTRY
-- inventory movement for each stock row to establish the initial audit trail.

-- =============================================================================
-- STOCK
-- =============================================================================

INSERT INTO stock (product_id, warehouse_id, quantity, min_quantity, updated_at) VALUES

    -- Electronics: Teclado Mecánico Redragon (product_id = 7)
    (7,  1,  40.00,  5.00, NOW()),
    (7,  2,  25.00,  5.00, NOW()),
    (7,  3,  20.00,  5.00, NOW()),

    -- Electronics: Monitor Samsung 24" (product_id = 8)
    (8,  1,  12.00,  2.00, NOW()),
    (8,  2,   8.00,  2.00, NOW()),
    (8,  3,   6.00,  2.00, NOW()),

    -- Electronics: Audífonos Sony WH-1000XM4 (product_id = 9)
    (9,  1,  18.00,  3.00, NOW()),
    (9,  2,  10.00,  3.00, NOW()),
    (9,  3,   8.00,  3.00, NOW()),

    -- Electronics: Disco Duro Externo 1TB (product_id = 10)
    (10, 1,  35.00,  5.00, NOW()),
    (10, 2,  20.00,  5.00, NOW()),
    (10, 3,  15.00,  5.00, NOW()),

    -- Electronics: Webcam Logitech C920 (product_id = 11)
    (11, 1,  22.00,  3.00, NOW()),
    (11, 2,  15.00,  3.00, NOW()),
    (11, 3,  12.00,  3.00, NOW()),

    -- Electronics: Hub USB-C 7 en 1 (product_id = 12)
    (12, 1,  50.00,  5.00, NOW()),
    (12, 2,  30.00,  5.00, NOW()),
    (12, 3,  25.00,  5.00, NOW()),

    -- Electronics: Tablet Samsung Galaxy A8 (product_id = 13)
    (13, 1,  10.00,  2.00, NOW()),
    (13, 2,   6.00,  2.00, NOW()),
    (13, 3,   5.00,  2.00, NOW()),

    -- Food: Azúcar Manuelita 5kg (product_id = 14)
    (14, 1, 180.00, 20.00, NOW()),
    (14, 2, 120.00, 20.00, NOW()),
    (14, 3, 100.00, 20.00, NOW()),

    -- Food: Leche Entera Alquería 1L (product_id = 15)
    (15, 1, 250.00, 30.00, NOW()),
    (15, 2, 180.00, 30.00, NOW()),
    (15, 3, 150.00, 30.00, NOW()),

    -- Food: Harina de Trigo 1kg (product_id = 16)
    (16, 1, 200.00, 25.00, NOW()),
    (16, 2, 140.00, 25.00, NOW()),
    (16, 3, 120.00, 25.00, NOW()),

    -- Food: Pasta Spaghetti 500g (product_id = 17)
    (17, 1, 300.00, 30.00, NOW()),
    (17, 2, 200.00, 30.00, NOW()),
    (17, 3, 160.00, 30.00, NOW()),

    -- Food: Sal Marina 1kg (product_id = 18)
    (18, 1, 220.00, 25.00, NOW()),
    (18, 2, 160.00, 25.00, NOW()),
    (18, 3, 130.00, 25.00, NOW()),

    -- Food: Café Molido Águila 500g (product_id = 19)
    (19, 1, 150.00, 15.00, NOW()),
    (19, 2, 100.00, 15.00, NOW()),
    (19, 3,  80.00, 15.00, NOW()),

    -- Food: Atún en Lata Van Camps 170g (product_id = 20)
    (20, 1, 400.00, 40.00, NOW()),
    (20, 2, 280.00, 40.00, NOW()),
    (20, 3, 220.00, 40.00, NOW()),

    -- Cleaning: Blanqueador Concentrado 1L (product_id = 21)
    (21, 1,  80.00, 10.00, NOW()),
    (21, 2,  55.00, 10.00, NOW()),
    (21, 3,  45.00, 10.00, NOW()),

    -- Cleaning: Limpiapisos Fabuloso 1L (product_id = 22)
    (22, 1,  70.00, 10.00, NOW()),
    (22, 2,  50.00, 10.00, NOW()),
    (22, 3,  40.00, 10.00, NOW()),

    -- Cleaning: Esponja Scotch-Brite x3 (product_id = 23)
    (23, 1, 120.00, 15.00, NOW()),
    (23, 2,  85.00, 15.00, NOW()),
    (23, 3,  70.00, 15.00, NOW()),

    -- Cleaning: Detergente en Polvo 2kg (product_id = 24)
    (24, 1,  90.00, 10.00, NOW()),
    (24, 2,  65.00, 10.00, NOW()),
    (24, 3,  55.00, 10.00, NOW()),

    -- Cleaning: Limpiavidrios Cristalín 750ml (product_id = 25)
    (25, 1,  75.00, 10.00, NOW()),
    (25, 2,  50.00, 10.00, NOW()),
    (25, 3,  40.00, 10.00, NOW()),

    -- Cleaning: Guantes de Látex Talla M x2 (product_id = 26)
    (26, 1, 100.00, 10.00, NOW()),
    (26, 2,  70.00, 10.00, NOW()),
    (26, 3,  60.00, 10.00, NOW());

-- =============================================================================
-- INVENTORY MOVEMENTS
-- =============================================================================

INSERT INTO inventory_movements (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at) VALUES

    -- Teclado Mecánico Redragon (product_id = 7, supplier_id = 1)
    (7,  1, 1, 1, 'PURCHASE_ENTRY',  40.00, 0.00,  40.00, 'Stock inicial Teclado Mecánico Redragon - Bodega Principal', NOW()),
    (7,  2, 1, 1, 'PURCHASE_ENTRY',  25.00, 0.00,  25.00, 'Stock inicial Teclado Mecánico Redragon - Bodega Norte',     NOW()),
    (7,  3, 1, 1, 'PURCHASE_ENTRY',  20.00, 0.00,  20.00, 'Stock inicial Teclado Mecánico Redragon - Bodega Sur',       NOW()),

    -- Monitor Samsung 24" (product_id = 8, supplier_id = 1)
    (8,  1, 1, 1, 'PURCHASE_ENTRY',  12.00, 0.00,  12.00, 'Stock inicial Monitor Samsung 24" - Bodega Principal', NOW()),
    (8,  2, 1, 1, 'PURCHASE_ENTRY',   8.00, 0.00,   8.00, 'Stock inicial Monitor Samsung 24" - Bodega Norte',     NOW()),
    (8,  3, 1, 1, 'PURCHASE_ENTRY',   6.00, 0.00,   6.00, 'Stock inicial Monitor Samsung 24" - Bodega Sur',       NOW()),

    -- Audífonos Sony WH-1000XM4 (product_id = 9, supplier_id = 1)
    (9,  1, 1, 1, 'PURCHASE_ENTRY',  18.00, 0.00,  18.00, 'Stock inicial Audífonos Sony WH-1000XM4 - Bodega Principal', NOW()),
    (9,  2, 1, 1, 'PURCHASE_ENTRY',  10.00, 0.00,  10.00, 'Stock inicial Audífonos Sony WH-1000XM4 - Bodega Norte',     NOW()),
    (9,  3, 1, 1, 'PURCHASE_ENTRY',   8.00, 0.00,   8.00, 'Stock inicial Audífonos Sony WH-1000XM4 - Bodega Sur',       NOW()),

    -- Disco Duro Externo 1TB (product_id = 10, supplier_id = 1)
    (10, 1, 1, 1, 'PURCHASE_ENTRY',  35.00, 0.00,  35.00, 'Stock inicial Disco Duro Externo 1TB - Bodega Principal', NOW()),
    (10, 2, 1, 1, 'PURCHASE_ENTRY',  20.00, 0.00,  20.00, 'Stock inicial Disco Duro Externo 1TB - Bodega Norte',     NOW()),
    (10, 3, 1, 1, 'PURCHASE_ENTRY',  15.00, 0.00,  15.00, 'Stock inicial Disco Duro Externo 1TB - Bodega Sur',       NOW()),

    -- Webcam Logitech C920 (product_id = 11, supplier_id = 1)
    (11, 1, 1, 1, 'PURCHASE_ENTRY',  22.00, 0.00,  22.00, 'Stock inicial Webcam Logitech C920 - Bodega Principal', NOW()),
    (11, 2, 1, 1, 'PURCHASE_ENTRY',  15.00, 0.00,  15.00, 'Stock inicial Webcam Logitech C920 - Bodega Norte',     NOW()),
    (11, 3, 1, 1, 'PURCHASE_ENTRY',  12.00, 0.00,  12.00, 'Stock inicial Webcam Logitech C920 - Bodega Sur',       NOW()),

    -- Hub USB-C 7 en 1 (product_id = 12, supplier_id = 1)
    (12, 1, 1, 1, 'PURCHASE_ENTRY',  50.00, 0.00,  50.00, 'Stock inicial Hub USB-C 7 en 1 - Bodega Principal', NOW()),
    (12, 2, 1, 1, 'PURCHASE_ENTRY',  30.00, 0.00,  30.00, 'Stock inicial Hub USB-C 7 en 1 - Bodega Norte',     NOW()),
    (12, 3, 1, 1, 'PURCHASE_ENTRY',  25.00, 0.00,  25.00, 'Stock inicial Hub USB-C 7 en 1 - Bodega Sur',       NOW()),

    -- Tablet Samsung Galaxy A8 (product_id = 13, supplier_id = 1)
    (13, 1, 1, 1, 'PURCHASE_ENTRY',  10.00, 0.00,  10.00, 'Stock inicial Tablet Samsung Galaxy A8 - Bodega Principal', NOW()),
    (13, 2, 1, 1, 'PURCHASE_ENTRY',   6.00, 0.00,   6.00, 'Stock inicial Tablet Samsung Galaxy A8 - Bodega Norte',     NOW()),
    (13, 3, 1, 1, 'PURCHASE_ENTRY',   5.00, 0.00,   5.00, 'Stock inicial Tablet Samsung Galaxy A8 - Bodega Sur',       NOW()),

    -- Azúcar Manuelita 5kg (product_id = 14, supplier_id = 2)
    (14, 1, 2, 1, 'PURCHASE_ENTRY', 180.00, 0.00, 180.00, 'Stock inicial Azúcar Manuelita 5kg - Bodega Principal', NOW()),
    (14, 2, 2, 1, 'PURCHASE_ENTRY', 120.00, 0.00, 120.00, 'Stock inicial Azúcar Manuelita 5kg - Bodega Norte',     NOW()),
    (14, 3, 2, 1, 'PURCHASE_ENTRY', 100.00, 0.00, 100.00, 'Stock inicial Azúcar Manuelita 5kg - Bodega Sur',       NOW()),

    -- Leche Entera Alquería 1L (product_id = 15, supplier_id = 2)
    (15, 1, 2, 1, 'PURCHASE_ENTRY', 250.00, 0.00, 250.00, 'Stock inicial Leche Entera Alquería 1L - Bodega Principal', NOW()),
    (15, 2, 2, 1, 'PURCHASE_ENTRY', 180.00, 0.00, 180.00, 'Stock inicial Leche Entera Alquería 1L - Bodega Norte',     NOW()),
    (15, 3, 2, 1, 'PURCHASE_ENTRY', 150.00, 0.00, 150.00, 'Stock inicial Leche Entera Alquería 1L - Bodega Sur',       NOW()),

    -- Harina de Trigo 1kg (product_id = 16, supplier_id = 2)
    (16, 1, 2, 1, 'PURCHASE_ENTRY', 200.00, 0.00, 200.00, 'Stock inicial Harina de Trigo 1kg - Bodega Principal', NOW()),
    (16, 2, 2, 1, 'PURCHASE_ENTRY', 140.00, 0.00, 140.00, 'Stock inicial Harina de Trigo 1kg - Bodega Norte',     NOW()),
    (16, 3, 2, 1, 'PURCHASE_ENTRY', 120.00, 0.00, 120.00, 'Stock inicial Harina de Trigo 1kg - Bodega Sur',       NOW()),

    -- Pasta Spaghetti 500g (product_id = 17, supplier_id = 2)
    (17, 1, 2, 1, 'PURCHASE_ENTRY', 300.00, 0.00, 300.00, 'Stock inicial Pasta Spaghetti 500g - Bodega Principal', NOW()),
    (17, 2, 2, 1, 'PURCHASE_ENTRY', 200.00, 0.00, 200.00, 'Stock inicial Pasta Spaghetti 500g - Bodega Norte',     NOW()),
    (17, 3, 2, 1, 'PURCHASE_ENTRY', 160.00, 0.00, 160.00, 'Stock inicial Pasta Spaghetti 500g - Bodega Sur',       NOW()),

    -- Sal Marina 1kg (product_id = 18, supplier_id = 2)
    (18, 1, 2, 1, 'PURCHASE_ENTRY', 220.00, 0.00, 220.00, 'Stock inicial Sal Marina 1kg - Bodega Principal', NOW()),
    (18, 2, 2, 1, 'PURCHASE_ENTRY', 160.00, 0.00, 160.00, 'Stock inicial Sal Marina 1kg - Bodega Norte',     NOW()),
    (18, 3, 2, 1, 'PURCHASE_ENTRY', 130.00, 0.00, 130.00, 'Stock inicial Sal Marina 1kg - Bodega Sur',       NOW()),

    -- Café Molido Águila 500g (product_id = 19, supplier_id = 2)
    (19, 1, 2, 1, 'PURCHASE_ENTRY', 150.00, 0.00, 150.00, 'Stock inicial Café Molido Águila 500g - Bodega Principal', NOW()),
    (19, 2, 2, 1, 'PURCHASE_ENTRY', 100.00, 0.00, 100.00, 'Stock inicial Café Molido Águila 500g - Bodega Norte',     NOW()),
    (19, 3, 2, 1, 'PURCHASE_ENTRY',  80.00, 0.00,  80.00, 'Stock inicial Café Molido Águila 500g - Bodega Sur',       NOW()),

    -- Atún en Lata Van Camps 170g (product_id = 20, supplier_id = 2)
    (20, 1, 2, 1, 'PURCHASE_ENTRY', 400.00, 0.00, 400.00, 'Stock inicial Atún en Lata Van Camps 170g - Bodega Principal', NOW()),
    (20, 2, 2, 1, 'PURCHASE_ENTRY', 280.00, 0.00, 280.00, 'Stock inicial Atún en Lata Van Camps 170g - Bodega Norte',     NOW()),
    (20, 3, 2, 1, 'PURCHASE_ENTRY', 220.00, 0.00, 220.00, 'Stock inicial Atún en Lata Van Camps 170g - Bodega Sur',       NOW()),

    -- Blanqueador Concentrado 1L (product_id = 21, supplier_id = 3)
    (21, 1, 3, 1, 'PURCHASE_ENTRY',  80.00, 0.00,  80.00, 'Stock inicial Blanqueador Concentrado 1L - Bodega Principal', NOW()),
    (21, 2, 3, 1, 'PURCHASE_ENTRY',  55.00, 0.00,  55.00, 'Stock inicial Blanqueador Concentrado 1L - Bodega Norte',     NOW()),
    (21, 3, 3, 1, 'PURCHASE_ENTRY',  45.00, 0.00,  45.00, 'Stock inicial Blanqueador Concentrado 1L - Bodega Sur',       NOW()),

    -- Limpiapisos Fabuloso 1L (product_id = 22, supplier_id = 3)
    (22, 1, 3, 1, 'PURCHASE_ENTRY',  70.00, 0.00,  70.00, 'Stock inicial Limpiapisos Fabuloso 1L - Bodega Principal', NOW()),
    (22, 2, 3, 1, 'PURCHASE_ENTRY',  50.00, 0.00,  50.00, 'Stock inicial Limpiapisos Fabuloso 1L - Bodega Norte',     NOW()),
    (22, 3, 3, 1, 'PURCHASE_ENTRY',  40.00, 0.00,  40.00, 'Stock inicial Limpiapisos Fabuloso 1L - Bodega Sur',       NOW()),

    -- Esponja Scotch-Brite x3 (product_id = 23, supplier_id = 3)
    (23, 1, 3, 1, 'PURCHASE_ENTRY', 120.00, 0.00, 120.00, 'Stock inicial Esponja Scotch-Brite x3 - Bodega Principal', NOW()),
    (23, 2, 3, 1, 'PURCHASE_ENTRY',  85.00, 0.00,  85.00, 'Stock inicial Esponja Scotch-Brite x3 - Bodega Norte',     NOW()),
    (23, 3, 3, 1, 'PURCHASE_ENTRY',  70.00, 0.00,  70.00, 'Stock inicial Esponja Scotch-Brite x3 - Bodega Sur',       NOW()),

    -- Detergente en Polvo 2kg (product_id = 24, supplier_id = 3)
    (24, 1, 3, 1, 'PURCHASE_ENTRY',  90.00, 0.00,  90.00, 'Stock inicial Detergente en Polvo 2kg - Bodega Principal', NOW()),
    (24, 2, 3, 1, 'PURCHASE_ENTRY',  65.00, 0.00,  65.00, 'Stock inicial Detergente en Polvo 2kg - Bodega Norte',     NOW()),
    (24, 3, 3, 1, 'PURCHASE_ENTRY',  55.00, 0.00,  55.00, 'Stock inicial Detergente en Polvo 2kg - Bodega Sur',       NOW()),

    -- Limpiavidrios Cristalín 750ml (product_id = 25, supplier_id = 3)
    (25, 1, 3, 1, 'PURCHASE_ENTRY',  75.00, 0.00,  75.00, 'Stock inicial Limpiavidrios Cristalín 750ml - Bodega Principal', NOW()),
    (25, 2, 3, 1, 'PURCHASE_ENTRY',  50.00, 0.00,  50.00, 'Stock inicial Limpiavidrios Cristalín 750ml - Bodega Norte',     NOW()),
    (25, 3, 3, 1, 'PURCHASE_ENTRY',  40.00, 0.00,  40.00, 'Stock inicial Limpiavidrios Cristalín 750ml - Bodega Sur',       NOW()),

    -- Guantes de Látex Talla M x2 (product_id = 26, supplier_id = 3)
    (26, 1, 3, 1, 'PURCHASE_ENTRY', 100.00, 0.00, 100.00, 'Stock inicial Guantes de Látex Talla M x2 - Bodega Principal', NOW()),
    (26, 2, 3, 1, 'PURCHASE_ENTRY',  70.00, 0.00,  70.00, 'Stock inicial Guantes de Látex Talla M x2 - Bodega Norte',     NOW()),
    (26, 3, 3, 1, 'PURCHASE_ENTRY',  60.00, 0.00,  60.00, 'Stock inicial Guantes de Látex Talla M x2 - Bodega Sur',       NOW());
