package com.miapp.inventory_system.products.api.dto.category;

import jakarta.validation.constraints.NotBlank;

public record RegisterCategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name
) {}
