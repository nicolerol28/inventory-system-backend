package com.miapp.inventory_system.inventory.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        Long productId,
        Long warehouseId,
        BigDecimal quantity,
        BigDecimal minQuantity,
        boolean belowMinimum,
        LocalDateTime updatedAt
) {}