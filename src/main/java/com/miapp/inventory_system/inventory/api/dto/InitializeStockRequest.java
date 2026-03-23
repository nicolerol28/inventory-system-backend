package com.miapp.inventory_system.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record InitializeStockRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El id del producto debe ser positivo")
        Long productId,

        @NotNull(message = "El almacén es obligatorio")
        @Positive(message = "El id del almacén debe ser positivo")
        Long warehouseId,

        @NotNull(message = "La cantidad mínima es obligatoria")
        @PositiveOrZero(message = "La cantidad mínima no puede ser negativa")
        BigDecimal minQuantity
) {}
