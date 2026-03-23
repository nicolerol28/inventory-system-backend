package com.miapp.inventory_system.suppliers.api.dto;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String contact,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
