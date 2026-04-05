-- V7__add_image_url_to_products.sql
-- Adds an optional image_url column to the products table
-- to support product image display in the frontend.

ALTER TABLE products
    ADD COLUMN image_url VARCHAR(500);
