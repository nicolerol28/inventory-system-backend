package com.miapp.inventory_system.inventory.application.command;

import com.miapp.inventory_system.inventory.domain.model.MovementType;
import java.math.BigDecimal;

public record RegisterStockMovementCommand(
        Long productId,
        Long warehouseId,
        Long supplierId,
        Long registeredBy,
        MovementType movementType,
        BigDecimal quantity,
        String comment
) {}