package com.miapp.inventory_system.inventory.api.dto;

import com.miapp.inventory_system.inventory.domain.model.MovementType;
import java.math.BigDecimal;

public record RegisterStockMovementRequest(
        Long productId,
        Long warehouseId,
        Long supplierId,
        Long registeredBy,
        MovementType movementType,
        BigDecimal quantity,
        String comment
) {}