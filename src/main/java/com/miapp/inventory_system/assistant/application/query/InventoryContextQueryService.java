package com.miapp.inventory_system.assistant.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryContextQueryService {

    private final JdbcTemplate jdbcTemplate;

    private static final long CONTEXT_CACHE_TTL_MS = 30_000L;

    private volatile String cachedContext = null;
    private volatile long contextCachedAt = 0L;

    public String getContext() {
        long now = System.currentTimeMillis();
        if (cachedContext == null || (now - contextCachedAt) > CONTEXT_CACHE_TTL_MS) {
            cachedContext = buildContext();
            contextCachedAt = now;
        }
        return cachedContext;
    }

    private String buildContext() {
        Integer totalProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE active = true", Integer.class);

        Integer totalSuppliers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM suppliers WHERE active = true", Integer.class);

        Integer totalWarehouses = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warehouses WHERE active = true", Integer.class);

        Integer totalInactiveProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE active = false", Integer.class);

        Integer totalInactiveSuppliers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM suppliers WHERE active = false", Integer.class);

        Integer totalInactiveWarehouses = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warehouses WHERE active = false", Integer.class);

        List<Map<String, Object>> belowMin = jdbcTemplate.queryForList(
                "SELECT p.name, p.sku, s.quantity, s.min_quantity, w.name AS warehouse " +
                "FROM stock s " +
                "JOIN products p ON s.product_id = p.id " +
                "JOIN warehouses w ON s.warehouse_id = w.id " +
                "WHERE s.quantity < s.min_quantity AND p.active = true " +
                "LIMIT 10");

        List<Map<String, Object>> lastMovements = jdbcTemplate.queryForList(
                "SELECT p.name, im.movement_type, im.quantity, w.name AS warehouse, im.created_at " +
                "FROM inventory_movements im " +
                "JOIN products p ON im.product_id = p.id " +
                "JOIN warehouses w ON im.warehouse_id = w.id " +
                "ORDER BY im.created_at DESC " +
                "LIMIT 10");

        List<Map<String, Object>> topSellingProducts = jdbcTemplate.queryForList(
                "SELECT p.name, SUM(im.quantity) as total " +
                "FROM inventory_movements im " +
                "JOIN products p ON im.product_id = p.id " +
                "WHERE im.movement_type = 'SALE_EXIT' " +
                "GROUP BY p.name " +
                "ORDER BY total DESC " +
                "LIMIT 5");

        List<Map<String, Object>> bottomSellingProducts = jdbcTemplate.queryForList(
                "SELECT p.name, SUM(im.quantity) as total " +
                "FROM inventory_movements im " +
                "JOIN products p ON im.product_id = p.id " +
                "WHERE im.movement_type = 'SALE_EXIT' " +
                "GROUP BY p.name " +
                "ORDER BY total ASC " +
                "LIMIT 5");

        List<Map<String, Object>> topStockWarehouse = jdbcTemplate.queryForList(
                "SELECT w.name, SUM(s.quantity) as total " +
                "FROM stock s " +
                "JOIN warehouses w ON s.warehouse_id = w.id " +
                "GROUP BY w.name " +
                "ORDER BY total DESC " +
                "LIMIT 1");

        List<Map<String, Object>> stockPerWarehouse = jdbcTemplate.queryForList(
                "SELECT w.name, SUM(s.quantity) as total " +
                "FROM stock s " +
                "JOIN warehouses w ON s.warehouse_id = w.id " +
                "GROUP BY w.name " +
                "ORDER BY w.name ASC");

        List<Map<String, Object>> stockLocationMap = jdbcTemplate.queryForList(
                "SELECT p.name, p.sku, w.name as warehouse, s.quantity " +
                "FROM stock s " +
                "JOIN products p ON s.product_id = p.id " +
                "JOIN warehouses w ON s.warehouse_id = w.id " +
                "WHERE p.active = true " +
                "ORDER BY p.name, w.name");

        Integer movementsThisMonth = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_movements " +
                "WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                "AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)",
                Integer.class);

        java.math.BigDecimal totalInventoryValue = jdbcTemplate.queryForObject(
                "SELECT SUM(s.quantity * p.purchase_price) as total_value " +
                "FROM stock s " +
                "JOIN products p ON s.product_id = p.id " +
                "WHERE p.active = true",
                java.math.BigDecimal.class);

        List<Map<String, Object>> movementsByType = jdbcTemplate.queryForList(
                "SELECT movement_type, COUNT(*) as total " +
                "FROM inventory_movements " +
                "WHERE EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                "AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                "GROUP BY movement_type " +
                "ORDER BY total DESC");

        List<Map<String, Object>> deadStockProducts = jdbcTemplate.queryForList(
                "SELECT p.name, p.sku " +
                "FROM products p " +
                "WHERE p.active = true " +
                "AND p.id NOT IN ( " +
                "    SELECT DISTINCT product_id FROM inventory_movements " +
                "    WHERE created_at >= CURRENT_DATE - INTERVAL '90 days' " +
                ") " +
                "LIMIT 10");

        List<Map<String, Object>> topSupplier = jdbcTemplate.queryForList(
                "SELECT s.name, COUNT(p.id) as total " +
                "FROM suppliers s " +
                "JOIN products p ON p.supplier_id = s.id " +
                "WHERE p.active = true " +
                "GROUP BY s.name " +
                "ORDER BY total DESC " +
                "LIMIT 1");

        List<Map<String, Object>> mostExpensiveProduct = jdbcTemplate.queryForList(
                "SELECT name, sale_price FROM products WHERE active = true ORDER BY sale_price DESC LIMIT 1");

        List<Map<String, Object>> cheapestProduct = jdbcTemplate.queryForList(
                "SELECT name, sale_price FROM products WHERE active = true ORDER BY sale_price ASC LIMIT 1");

        StringBuilder belowMinStr = new StringBuilder();
        if (belowMin.isEmpty()) {
            belowMinStr.append("Ninguno");
        } else {
            for (Map<String, Object> row : belowMin) {
                belowMinStr.append(String.format("%n  - %s (%s) en %s: stock=%s, mínimo=%s",
                        row.get("name"), row.get("sku"), row.get("warehouse"),
                        row.get("quantity"), row.get("min_quantity")));
            }
        }

        StringBuilder movementsStr = new StringBuilder();
        for (Map<String, Object> row : lastMovements) {
            movementsStr.append(String.format("%n  - %s | %s | cantidad: %s | bodega: %s | fecha: %s",
                    row.get("name"), row.get("movement_type"), row.get("quantity"),
                    row.get("warehouse"), row.get("created_at")));
        }

        StringBuilder topSellingStr = new StringBuilder();
        if (topSellingProducts.isEmpty()) {
            topSellingStr.append("Sin datos");
        } else {
            for (Map<String, Object> row : topSellingProducts) {
                topSellingStr.append(String.format("%n  - %s: %s unidades",
                        row.get("name"), row.get("total")));
            }
        }

        StringBuilder bottomSellingStr = new StringBuilder();
        if (bottomSellingProducts.isEmpty()) {
            bottomSellingStr.append("Sin datos");
        } else {
            for (Map<String, Object> row : bottomSellingProducts) {
                bottomSellingStr.append(String.format("%n  - %s: %s unidades",
                        row.get("name"), row.get("total")));
            }
        }

        String topStockWarehouseStr = topStockWarehouse.isEmpty()
                ? "Sin datos"
                : String.format("%s (%s unidades)", topStockWarehouse.get(0).get("name"), topStockWarehouse.get(0).get("total"));

        StringBuilder stockPerWarehouseStr = new StringBuilder();
        if (stockPerWarehouse.isEmpty()) {
            stockPerWarehouseStr.append("Sin datos");
        } else {
            for (Map<String, Object> row : stockPerWarehouse) {
                stockPerWarehouseStr.append(String.format("%n  - %s: %s unidades",
                        row.get("name"), row.get("total")));
            }
        }

        StringBuilder stockLocationMapStr = new StringBuilder();
        if (stockLocationMap.isEmpty()) {
            stockLocationMapStr.append("Sin datos");
        } else {
            for (Map<String, Object> row : stockLocationMap) {
                stockLocationMapStr.append(String.format("%n  - %s (%s): %s %s unidades",
                        row.get("name"), row.get("sku"), row.get("warehouse"), row.get("quantity")));
            }
        }

        String totalInventoryValueStr = totalInventoryValue == null ? "Sin datos" : "$" + totalInventoryValue;

        StringBuilder movementsByTypeStr = new StringBuilder();
        if (movementsByType.isEmpty()) {
            movementsByTypeStr.append("Sin datos");
        } else {
            for (Map<String, Object> row : movementsByType) {
                movementsByTypeStr.append(String.format("%n  - %s: %s",
                        row.get("movement_type"), row.get("total")));
            }
        }

        StringBuilder deadStockStr = new StringBuilder();
        if (deadStockProducts.isEmpty()) {
            deadStockStr.append("Ninguno");
        } else {
            for (Map<String, Object> row : deadStockProducts) {
                deadStockStr.append(String.format("%n  - %s (%s)",
                        row.get("name"), row.get("sku")));
            }
        }

        String topSupplierStr = topSupplier.isEmpty()
                ? "Sin datos"
                : String.format("%s (%s productos)", topSupplier.get(0).get("name"), topSupplier.get(0).get("total"));

        String mostExpensiveStr = mostExpensiveProduct.isEmpty()
                ? "Sin datos"
                : String.format("%s ($%s)", mostExpensiveProduct.get(0).get("name"), mostExpensiveProduct.get(0).get("sale_price"));

        String cheapestStr = cheapestProduct.isEmpty()
                ? "Sin datos"
                : String.format("%s ($%s)", cheapestProduct.get(0).get("name"), cheapestProduct.get(0).get("sale_price"));

        return String.format(
                "- Total productos activos: %d%n" +
                "- Total proveedores activos: %d%n" +
                "- Total almacenes activos: %d%n" +
                "- Productos inactivos: %d%n" +
                "- Proveedores inactivos: %d%n" +
                "- Almacenes inactivos: %d%n" +
                "- Productos bajo mínimo: %s%n" +
                "- Últimos movimientos: %s%n" +
                "- Productos más vendidos (por cantidad total de salidas de venta): %s%n" +
                "- Productos menos vendidos (por cantidad total de salidas de venta): %s%n" +
                "- Almacén con más stock: %s%n" +
                "- Stock por almacén: %s%n" +
                "- Ubicación de productos por almacén: %s%n" +
                "- Movimientos este mes: %d%n" +
                "- Valor total del inventario: %s%n" +
                "- Movimientos por tipo este mes: %s%n" +
                "- Productos sin movimientos en los últimos 90 días (posible stock muerto): %s%n" +
                "- Proveedor con más productos activos: %s%n" +
                "- Producto más caro: %s%n" +
                "- Producto más barato: %s",
                totalProducts, totalSuppliers, totalWarehouses,
                totalInactiveProducts, totalInactiveSuppliers, totalInactiveWarehouses,
                belowMinStr, movementsStr, topSellingStr, bottomSellingStr,
                topStockWarehouseStr, stockPerWarehouseStr, stockLocationMapStr, movementsThisMonth,
                totalInventoryValueStr, movementsByTypeStr, deadStockStr, topSupplierStr,
                mostExpensiveStr, cheapestStr);
    }
}
