package com.miapp.inventory_system.products.application.command.product;

import java.math.BigDecimal;
import java.util.Optional;

public record UpdateProductCommand(
        Long id,
        String name,
        String description,
        String sku,
        Long unitId,
        Long categoryId,
        Long supplierId,
        Optional<BigDecimal> purchasePrice,
        Optional<BigDecimal> salePrice,
        Optional<String> imageUrl
){}
