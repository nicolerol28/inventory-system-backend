package com.miapp.inventory_system.products.api.dto.category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        boolean active,
        LocalDateTime createdAt
) {}
