package com.miapp.inventory_system.warehouse.domain;

public interface StockChecker {
    boolean hasActiveStockByWarehouseId(Long warehouseId);
}