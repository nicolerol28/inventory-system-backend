package com.miapp.inventory_system.inventory.api.dto;

import com.miapp.inventory_system.inventory.domain.model.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegisterStockMovementRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El id del producto debe ser positivo")
        Long productId,

        @NotNull(message = "El almacén es obligatorio")
        @Positive(message = "El id del almacén debe ser positivo")
        Long warehouseId,

        Long supplierId,

        @NotNull(message = "El usuario es obligatorio")
        @Positive(message = "El id del usuario debe ser positivo")
        Long registeredBy,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        MovementType movementType,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal quantity,
        String comment
) {}