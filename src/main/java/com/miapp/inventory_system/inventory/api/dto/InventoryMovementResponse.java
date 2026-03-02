package com.miapp.inventory_system.inventory.api.dto;

import com.miapp.inventory_system.inventory.domain.model.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        Long warehouseId,
        Long supplierId,
        Long registeredBy,
        MovementType movementType,
        BigDecimal quantity,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        String comment,
        LocalDateTime createdAt
) {}