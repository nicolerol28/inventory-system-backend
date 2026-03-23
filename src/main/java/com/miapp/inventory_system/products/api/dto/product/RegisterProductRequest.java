package com.miapp.inventory_system.products.api.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Optional;

public record RegisterProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

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

        Optional<BigDecimal> purchasePrice,
        Optional<BigDecimal> salePrice
) {}