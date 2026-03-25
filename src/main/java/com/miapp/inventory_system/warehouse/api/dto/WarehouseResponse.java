package com.miapp.inventory_system.warehouse.api.dto;

import java.time.LocalDateTime;

public record WarehouseResponse(
        Long id,
        String name,
        String location,
        Boolean active,
        LocalDateTime createdAt
){}
