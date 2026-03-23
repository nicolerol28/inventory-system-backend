package com.miapp.inventory_system.products.api.dto.unit;

import jakarta.validation.constraints.NotBlank;

public record RegisterUnitRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El símbolo es obligatorio")
        String symbol
) {}