package com.miapp.inventory_system.warehouse.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterWarehouseRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String location
){}
