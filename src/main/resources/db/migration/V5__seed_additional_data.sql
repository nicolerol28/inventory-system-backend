-- V5__seed_additional_data.sql
-- Adds supplementary seed data for demo/testing purposes:
--   Part 1: Updates 6 stock records so that quantity falls below min_quantity,
--            simulating low-stock alerts across the 3 warehouses and all 3 categories.
--   Part 2: Inserts inactive suppliers, warehouses, and products to populate
--            the "inactive records" view in the frontend.
--   Part 3: Inserts 31 inventory movements covering all 5 movement types
--            (SALE_EXIT, DAMAGE_EXIT, ADJUSTMENT_OUT, ADJUSTMENT_IN, RETURN_ENTRY).

-- ============================================================
-- PART 1 — Update stock records to simulate below-minimum stock
-- ============================================================

-- TECH-001 / Bodega Principal: quantity 15 -> 2  (min_quantity = 3)
UPDATE stock
SET quantity = 2.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'TECH-001')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Principal');

-- LIMP-001 / Bodega Norte: quantity 8 -> 3  (min_quantity = 5)
-- Note: LIMP-001 maps to product at (product_id, warehouse_id) = (LIMP-001, Bodega Norte)
--       Original V4 stock for LIMP-001/Norte was (21,2) qty=55. The row referenced below
--       uses subqueries so no hardcoded IDs are needed.
UPDATE stock
SET quantity = 3.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'LIMP-001')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Norte');

-- ALIM-001 / Bodega Principal: quantity 35 -> 12  (min_quantity = 20)
UPDATE stock
SET quantity = 12.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'ALIM-001')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Principal');

-- TECH-004 / Bodega Sur: quantity 80 -> 1  (min_quantity = 8)
-- The spec requests min=2; however V4 seeded min_quantity=8 for this record.
-- Setting quantity=1 ensures quantity < min_quantity regardless of the stored value.
UPDATE stock
SET quantity = 1.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'TECH-004')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Sur');

-- ALIM-008 / Bodega Norte: quantity 65 -> 8  (min_quantity = 15 per V4 seed)
UPDATE stock
SET quantity = 8.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'ALIM-008')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Norte');

-- LIMP-006 / Bodega Sur: quantity 40 -> 6  (min_quantity = 10 per V4 seed)
UPDATE stock
SET quantity = 6.00
WHERE product_id = (SELECT id FROM products WHERE sku = 'LIMP-006')
  AND warehouse_id = (SELECT id FROM warehouses WHERE name = 'Bodega Sur');

-- ============================================================
-- PART 2 — Inactive records (suppliers, warehouses, products)
-- ============================================================

-- Inactive suppliers
INSERT INTO suppliers (name, contact, phone, active, created_at, updated_at) VALUES
    ('Importaciones Globales S.A.', 'Pedro Gómez',   '3012223344', FALSE, NOW(), NOW()),
    ('Suministros Rápidos Ltda.',   'Sandra Ríos',   '3187654321', FALSE, NOW(), NOW());

-- Inactive warehouses
INSERT INTO warehouses (name, location, active, created_at) VALUES
    ('Bodega Occidente', 'Pereira, Risaralda', FALSE, NOW()),
    ('Bodega Centro',    'Manizales, Caldas',  FALSE, NOW());

-- Inactive products
INSERT INTO products (name, description, sku, unit_id, purchase_price, sale_price, category_id, supplier_id, active, created_at, updated_at) VALUES
    (
        'Impresora HP LaserJet',
        'Impresora láser monocromática',
        'TECH-010',
        (SELECT id FROM units      WHERE symbol = 'UND'),
        850000.00,
        1150000.00,
        (SELECT id FROM categories WHERE name = 'Electrónica'),
        (SELECT id FROM suppliers  WHERE name  = 'TechSupplies S.A.S'),
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'Panela 500g',
        'Panela de caña de azúcar sin refinar',
        'ALIM-010',
        (SELECT id FROM units      WHERE symbol = 'KG'),
        3500.00,
        5500.00,
        (SELECT id FROM categories WHERE name = 'Alimentos'),
        (SELECT id FROM suppliers  WHERE name  = 'Distribuidora Alimentos del Valle'),
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'Escoba Plástica Ref. 40',
        'Escoba con mango plástico reforzado',
        'LIMP-009',
        (SELECT id FROM units      WHERE symbol = 'UND'),
        12000.00,
        19000.00,
        (SELECT id FROM categories WHERE name = 'Limpieza'),
        (SELECT id FROM suppliers  WHERE name  = 'Productos Limpios Ltda'),
        FALSE,
        NOW(),
        NOW()
    );

-- ============================================================
-- PART 3 — Inventory movements
-- registered_by = 1 (admin, always id=1 per BIGSERIAL in V2)
-- ============================================================

-- ---------------------------
-- SALE_EXIT (6 movements)
-- ---------------------------
INSERT INTO inventory_movements
    (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at)
VALUES
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'SALE_EXIT',
        10.00, 50.00, 40.00,
        'Venta a cliente corporativo',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'SALE_EXIT',
        15.00, 100.00, 85.00,
        'Venta directa aceite',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'SALE_EXIT',
        20.00, 90.00, 70.00,
        'Venta a supermercado',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-003'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'SALE_EXIT',
        5.00, 25.00, 20.00,
        'Venta teclados Bogotá',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-003'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'SALE_EXIT',
        30.00, 180.00, 150.00,
        'Venta azúcar distribuidora',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-004'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'SALE_EXIT',
        8.00, 50.00, 42.00,
        'Venta limpiapisos cliente retail',
        NOW()
    );

-- ---------------------------
-- DAMAGE_EXIT (6 movements)
-- ---------------------------
INSERT INTO inventory_movements
    (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at)
VALUES
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-005'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'DAMAGE_EXIT',
        3.00, 18.00, 15.00,
        'Daño en transporte audífonos',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-004'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'DAMAGE_EXIT',
        10.00, 250.00, 240.00,
        'Leche vencida retirada',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-003'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'DAMAGE_EXIT',
        8.00, 55.00, 47.00,
        'Blanqueador derramado almacén',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-005'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'DAMAGE_EXIT',
        5.00, 70.00, 65.00,
        'Esponjas mojadas dañadas',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-006'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'DAMAGE_EXIT',
        4.00, 20.00, 16.00,
        'Discos duros con fallo de lectura',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-005'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'DAMAGE_EXIT',
        7.00, 120.00, 113.00,
        'Harina con humedad retirada',
        NOW()
    );

-- ---------------------------
-- ADJUSTMENT_OUT (6 movements)
-- ---------------------------
INSERT INTO inventory_movements
    (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at)
VALUES
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-007'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        3.00, 22.00, 19.00,
        'Ajuste inventario webcam',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-009'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        20.00, 280.00, 260.00,
        'Ajuste conteo atún',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-004'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        5.00, 40.00, 35.00,
        'Ajuste limpiapisos bodega sur',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-007'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        10.00, 75.00, 65.00,
        'Corrección inventario limpiavidrios',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-009'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        1.00, 6.00, 5.00,
        'Tablet con pantalla rota ajuste',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-006'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'ADJUSTMENT_OUT',
        15.00, 200.00, 185.00,
        'Ajuste pasta diferencia conteo',
        NOW()
    );

-- ---------------------------
-- ADJUSTMENT_IN (6 movements)
-- ---------------------------
INSERT INTO inventory_movements
    (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at)
VALUES
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-008'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        10.00, 50.00, 60.00,
        'Ajuste positivo hub USB-C',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-007'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        20.00, 160.00, 180.00,
        'Ajuste sal marina conteo físico',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-005'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        15.00, 120.00, 135.00,
        'Ajuste esponjas encontradas',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        10.00, 80.00, 90.00,
        'Ajuste aceite girasol bodega sur',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        5.00, 70.00, 75.00,
        'Ajuste desinfectante conteo',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-003'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        NULL,
        1,
        'ADJUSTMENT_IN',
        25.00, 100.00, 125.00,
        'Reconteo azúcar diferencia positiva',
        NOW()
    );

-- ---------------------------
-- RETURN_ENTRY (7 movements, con supplier_id)
-- ---------------------------
INSERT INTO inventory_movements
    (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at)
VALUES
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-001'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        (SELECT id FROM suppliers  WHERE name = 'TechSupplies S.A.S'),
        1,
        'RETURN_ENTRY',
        3.00, 8.00, 11.00,
        'Devolución laptop defectuosa proveedor',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-002'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        (SELECT id FROM suppliers  WHERE name = 'TechSupplies S.A.S'),
        1,
        'RETURN_ENTRY',
        5.00, 30.00, 35.00,
        'Devolución mouse cliente',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-001'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Principal'),
        (SELECT id FROM suppliers  WHERE name = 'Productos Limpios Ltda'),
        1,
        'RETURN_ENTRY',
        4.00, 60.00, 64.00,
        'Devolución jabón exceso pedido',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-004'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        (SELECT id FROM suppliers  WHERE name = 'Distribuidora Alimentos del Valle'),
        1,
        'RETURN_ENTRY',
        10.00, 150.00, 160.00,
        'Devolución leche pedido incorrecto',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'TECH-003'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        (SELECT id FROM suppliers  WHERE name = 'TechSupplies S.A.S'),
        1,
        'RETURN_ENTRY',
        3.00, 20.00, 23.00,
        'Devolución teclados cambio modelo',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'ALIM-006'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Sur'),
        (SELECT id FROM suppliers  WHERE name = 'Distribuidora Alimentos del Valle'),
        1,
        'RETURN_ENTRY',
        20.00, 160.00, 180.00,
        'Devolución pasta spaghetti proveedor',
        NOW()
    ),
    (
        (SELECT id FROM products  WHERE sku  = 'LIMP-006'),
        (SELECT id FROM warehouses WHERE name = 'Bodega Norte'),
        (SELECT id FROM suppliers  WHERE name = 'Productos Limpios Ltda'),
        1,
        'RETURN_ENTRY',
        5.00, 65.00, 70.00,
        'Devolución detergente sobrestock',
        NOW()
    );
