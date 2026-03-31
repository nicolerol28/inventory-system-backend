package com.miapp.inventory_system.products.domain;

public interface ProductStockChecker {
    boolean hasActiveStockByProductId(Long productId);
}
