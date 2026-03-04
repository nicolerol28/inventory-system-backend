package com.miapp.inventory_system.inventory.api.dto;

import java.math.BigDecimal;

public record InitializeStockRequest(
        Long productId,
        Long warehouseId,
        BigDecimal minQuantity
) {}
