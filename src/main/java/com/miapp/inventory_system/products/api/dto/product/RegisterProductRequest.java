package com.miapp.inventory_system.products.api.dto.product;

import java.math.BigDecimal;
import java.util.Optional;

public record RegisterProductRequest(
        String name,
        String description,
        String sku,
        Long unitId,
        Long categoryId,
        Long supplierId,
        Optional<BigDecimal> purchasePrice,
        Optional<BigDecimal> salePrice
) {}