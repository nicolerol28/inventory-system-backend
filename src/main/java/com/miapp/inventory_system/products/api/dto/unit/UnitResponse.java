package com.miapp.inventory_system.products.api.dto.unit;

import java.time.LocalDateTime;

public record UnitResponse(
        Long id,
        String name,
        String symbol,
        boolean active,
        LocalDateTime createdAt
) {}