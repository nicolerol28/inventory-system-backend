package com.miapp.inventory_system.products.api.dto.unit;

import jakarta.validation.constraints.NotBlank;


public record UpdateUnitRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El símbolo es obligatorio")
        String symbol
) {}