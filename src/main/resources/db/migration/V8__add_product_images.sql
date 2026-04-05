-- V8__add_product_images.sql
-- Assigns R2 image URLs to all products.

-- Electrónica
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141537046.png'  WHERE sku = 'TECH-001';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141752333.png'  WHERE sku = 'TECH-002';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_142218984.png'  WHERE sku = 'TECH-003';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141708838.png'  WHERE sku = 'TECH-004';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_140805597.png'  WHERE sku = 'TECH-005';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141019434.png'  WHERE sku = 'TECH-006';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_142320724.png'  WHERE sku = 'TECH-007';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141230336.png'  WHERE sku = 'TECH-008';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_142108858.png'  WHERE sku = 'TECH-009';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_141338657.png'  WHERE sku = 'TECH-010';

-- Alimentos
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20142715.png' WHERE sku = 'ALIM-001';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/imagen_2026-04-05_142509490.png'                    WHERE sku = 'ALIM-002';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20142837.png' WHERE sku = 'ALIM-003';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143126.png' WHERE sku = 'ALIM-004';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143040.png' WHERE sku = 'ALIM-005';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143220.png' WHERE sku = 'ALIM-006';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143328.png' WHERE sku = 'ALIM-007';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20142924.png' WHERE sku = 'ALIM-008';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20142737.png' WHERE sku = 'ALIM-009';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143159.png' WHERE sku = 'ALIM-010';

-- Limpieza
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143748.png' WHERE sku = 'LIMP-001';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143729.png' WHERE sku = 'LIMP-002';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143703.png' WHERE sku = 'LIMP-003';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20144059.png' WHERE sku = 'LIMP-004';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143827.png' WHERE sku = 'LIMP-005';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20144044.png' WHERE sku = 'LIMP-006';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20144127.png' WHERE sku = 'LIMP-007';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20144030.png' WHERE sku = 'LIMP-008';
UPDATE products SET image_url = 'https://pub-fa8aac3841e74662b37f4022a5a15f53.r2.dev/products/Captura%20de%20pantalla%202026-04-05%20143813.png' WHERE sku = 'LIMP-009';