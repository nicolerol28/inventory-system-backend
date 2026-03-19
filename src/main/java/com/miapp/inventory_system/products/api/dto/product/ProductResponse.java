package com.miapp.inventory_system.products.api.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        Long unitId,
        Long categoryId,
        Long supplierId,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
