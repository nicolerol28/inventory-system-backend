-- Units
CREATE TABLE IF NOT EXISTS units (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    symbol      VARCHAR(10)  NOT NULL UNIQUE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    contact     VARCHAR(100),
    phone       VARCHAR(20),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Warehouses
CREATE TABLE IF NOT EXISTS warehouses (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    location    VARCHAR(200),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(50)   NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR')),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    description     TEXT,
    sku             VARCHAR(50)     NOT NULL UNIQUE,
    unit_id         BIGINT          NOT NULL REFERENCES units(id),
    purchase_price  NUMERIC(10,2),
    sale_price      NUMERIC(10,2),
    category_id     BIGINT          NOT NULL REFERENCES categories(id),
    supplier_id     BIGINT          REFERENCES suppliers(id),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Stock
CREATE TABLE IF NOT EXISTS stock (
    id            BIGSERIAL       PRIMARY KEY,
    product_id    BIGINT          NOT NULL REFERENCES products(id),
    warehouse_id  BIGINT          NOT NULL REFERENCES warehouses(id),
    quantity      NUMERIC(10,2)   NOT NULL DEFAULT 0,
    min_quantity  NUMERIC(10,2)   NOT NULL DEFAULT 0,
    updated_at    TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_stock_product_warehouse
        UNIQUE(product_id, warehouse_id)
);

-- Inventory movements
CREATE TABLE IF NOT EXISTS inventory_movements (
    id                BIGSERIAL       PRIMARY KEY,
    product_id        BIGINT          NOT NULL REFERENCES products(id),
    warehouse_id      BIGINT          NOT NULL REFERENCES warehouses(id),
    supplier_id       BIGINT          REFERENCES suppliers(id),
    registered_by     BIGINT          NOT NULL REFERENCES users(id),
    movement_type     VARCHAR(30)     NOT NULL CHECK (movement_type IN (
                                          'PURCHASE_ENTRY',
                                          'SALE_EXIT',
                                          'RETURN_ENTRY',
                                          'DAMAGE_EXIT',
                                          'ADJUSTMENT_IN',
                                          'ADJUSTMENT_OUT'
                                      )),
    quantity          NUMERIC(10,2)   NOT NULL,
    quantity_before   NUMERIC(10,2)   NOT NULL,
    quantity_after    NUMERIC(10,2)   NOT NULL,
    comment           TEXT,
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_quantity_after_non_negative
        CHECK (quantity_after >= 0)
);