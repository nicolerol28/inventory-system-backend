-- Units
INSERT INTO units (name, symbol, active, created_at) VALUES
    ('Unidad', 'UND', true, NOW()),
    ('Kilogramo', 'KG', true, NOW()),
    ('Litro', 'LT', true, NOW());

-- Categories
INSERT INTO categories (name, active, created_at) VALUES
    ('Electrónica', true, NOW()),
    ('Alimentos', true, NOW()),
    ('Limpieza', true, NOW());

-- Suppliers
INSERT INTO suppliers (name, contact, phone, active, created_at, updated_at) VALUES
    ('TechSupplies S.A.S', 'Carlos Mejía', '3001234567', true, NOW(), NOW()),
    ('Distribuidora Alimentos del Valle', 'María López', '3109876543', true, NOW(), NOW()),
    ('Productos Limpios Ltda', 'Juan Torres', '3205551234', true, NOW(), NOW());

-- Warehouses
INSERT INTO warehouses (name, location, active, created_at) VALUES
    ('Bodega Principal', 'Cali, Valle del Cauca', true, NOW()),
    ('Bodega Norte', 'Bogotá, Cundinamarca', true, NOW()),
    ('Bodega Sur', 'Medellín, Antioquia', true, NOW());

-- Products
INSERT INTO products (name, description, sku, unit_id, purchase_price, sale_price, category_id, supplier_id, active, created_at, updated_at) VALUES
    ('Laptop Lenovo IdeaPad', 'Laptop 15 pulgadas, 8GB RAM, 512GB SSD', 'TECH-001', 1, 2500000.00, 3200000.00, 1, 1, true, NOW(), NOW()),
    ('Mouse Inalámbrico Logitech', 'Mouse ergonómico con receptor USB', 'TECH-002', 1, 85000.00, 120000.00, 1, 1, true, NOW(), NOW()),
    ('Arroz Diana 5kg', 'Arroz blanco de grano largo', 'ALIM-001', 2, 18000.00, 24000.00, 2, 2, true, NOW(), NOW()),
    ('Aceite Girasol 1L', 'Aceite de girasol refinado', 'ALIM-002', 3, 12000.00, 16000.00, 2, 2, true, NOW(), NOW()),
    ('Jabón Líquido Multiusos', 'Jabón concentrado para superficies', 'LIMP-001', 3, 15000.00, 22000.00, 3, 3, true, NOW(), NOW()),
    ('Desinfectante Pino', 'Desinfectante con fragancia a pino', 'LIMP-002', 3, 8000.00, 13000.00, 3, 3, true, NOW(), NOW());

-- Stock (producto por almacén)
INSERT INTO stock (product_id, warehouse_id, quantity, min_quantity, updated_at) VALUES
    (1, 1, 15.00, 3.00, NOW()),
    (1, 2, 8.00, 2.00, NOW()),
    (2, 1, 50.00, 10.00, NOW()),
    (2, 3, 30.00, 5.00, NOW()),
    (3, 1, 200.00, 20.00, NOW()),
    (3, 2, 150.00, 15.00, NOW()),
    (4, 1, 100.00, 10.00, NOW()),
    (4, 3, 80.00, 8.00, NOW()),
    (5, 1, 60.00, 5.00, NOW()),
    (5, 2, 40.00, 5.00, NOW()),
    (6, 1, 90.00, 10.00, NOW()),
    (6, 3, 70.00, 8.00, NOW());

-- Inventory movements (usando el admin seed como registered_by = 1)
INSERT INTO inventory_movements (product_id, warehouse_id, supplier_id, registered_by, movement_type, quantity, quantity_before, quantity_after, comment, created_at) VALUES
    (1, 1, 1, 1, 'PURCHASE_ENTRY', 15.00, 0.00, 15.00, 'Compra inicial laptops', NOW()),
    (1, 2, 1, 1, 'PURCHASE_ENTRY', 8.00, 0.00, 8.00, 'Compra inicial laptops bodega norte', NOW()),
    (2, 1, 1, 1, 'PURCHASE_ENTRY', 50.00, 0.00, 50.00, 'Compra inicial mouse', NOW()),
    (2, 3, 1, 1, 'PURCHASE_ENTRY', 30.00, 0.00, 30.00, 'Compra inicial mouse bodega sur', NOW()),
    (3, 1, 2, 1, 'PURCHASE_ENTRY', 200.00, 0.00, 200.00, 'Compra inicial arroz', NOW()),
    (4, 1, 2, 1, 'PURCHASE_ENTRY', 100.00, 0.00, 100.00, 'Compra inicial aceite', NOW()),
    (5, 1, 3, 1, 'PURCHASE_ENTRY', 60.00, 0.00, 60.00, 'Compra inicial jabón', NOW()),
    (6, 1, 3, 1, 'PURCHASE_ENTRY', 90.00, 0.00, 90.00, 'Compra inicial desinfectante', NOW()),
    (3, 2, 2, 1, 'PURCHASE_ENTRY', 150.00, 0.00, 150.00, 'Compra inicial arroz bodega norte', NOW()),
    (4, 3, 2, 1, 'PURCHASE_ENTRY', 80.00, 0.00, 80.00, 'Compra inicial aceite bodega sur', NOW());

-- Users adicionales
INSERT INTO users (name, email, password, role, active, created_at, updated_at) VALUES
    ('Operador Bodega Principal', 'operador1@inventory.com', '$2a$12$RxgHyl0IDfS8OyAboCZyr.Keh8b6BNtiGdmms9y7dqr.92m1Jv4gm', 'OPERATOR', true, NOW(), NOW()),
    ('Operador Bodega Norte', 'operador2@inventory.com', '$2a$12$RxgHyl0IDfS8OyAboCZyr.Keh8b6BNtiGdmms9y7dqr.92m1Jv4gm', 'OPERATOR', true, NOW(), NOW()),
    ('Admin Secundario', 'admin2@inventory.com', '$2a$12$RxgHyl0IDfS8OyAboCZyr.Keh8b6BNtiGdmms9y7dqr.92m1Jv4gm', 'ADMIN', true, NOW(), NOW());