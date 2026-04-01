package com.miapp.inventory_system.products.api.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegisterProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Size(max = 150, message = "La descripción no puede superar los 150 caracteres")
        String description,

        @NotBlank(message = "El SKU es obligatorio")
        String sku,

        @NotNull(message = "La unidad es obligatoria")
        @Positive(message = "El id de la unidad debe ser positivo")
        Long unitId,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "El id de la categoría debe ser positivo")
        Long categoryId,

        @Positive(message = "El id del proveedor debe ser positivo")
        Long supplierId,

        BigDecimal purchasePrice,
        BigDecimal salePrice
) {}