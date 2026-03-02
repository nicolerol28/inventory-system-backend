package com.miapp.inventory_system.inventory.domain.model;

import com.miapp.inventory_system.inventory.domain.exception.InsufficientStockException;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class InventoryMovement {

    private Long id;
    private Long productId;
    private Long warehouseId;
    private Long supplierId;
    private Long registeredBy;
    private MovementType movementType;
    private BigDecimal quantity;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private String comment;
    private LocalDateTime createdAt;

    private InventoryMovement() {}

    public static InventoryMovement create(
            Long productId,
            Long warehouseId,
            Long supplierId,
            Long registeredBy,
            MovementType movementType,
            BigDecimal quantity,
            BigDecimal currentStock,
            String comment) {

        validate(quantity, currentStock, movementType);

        InventoryMovement movement = new InventoryMovement();
        movement.productId      = productId;
        movement.warehouseId    = warehouseId;
        movement.supplierId     = supplierId;
        movement.registeredBy   = registeredBy;
        movement.movementType   = movementType;
        movement.quantity       = quantity;
        movement.quantityBefore = currentStock;
        movement.quantityAfter  = calculateNewStock(movementType, quantity, currentStock);
        movement.comment        = comment;
        movement.createdAt      = LocalDateTime.now();

        return movement;
    }

    private static void validate(
            BigDecimal quantity,
            BigDecimal currentStock,
            MovementType movementType) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad del movimiento debe ser mayor a cero");
        }

        if (!movementType.isInbound()) {
            BigDecimal newStock = currentStock.subtract(quantity);
            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientStockException(
                        "Stock insuficiente. Stock actual: " + currentStock +
                                ", cantidad solicitada: " + quantity);
            }
        }
    }

    private static BigDecimal calculateNewStock(
            MovementType movementType,
            BigDecimal quantity,
            BigDecimal currentStock) {

        return movementType.isInbound()
                ? currentStock.add(quantity)
                : currentStock.subtract(quantity);
    }

    public static InventoryMovement reconstitute(
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
            LocalDateTime createdAt) {

        InventoryMovement movement  = new InventoryMovement();
        movement.id                 = id;
        movement.productId          = productId;
        movement.warehouseId        = warehouseId;
        movement.supplierId         = supplierId;
        movement.registeredBy       = registeredBy;
        movement.movementType       = movementType;
        movement.quantity           = quantity;
        movement.quantityBefore     = quantityBefore;
        movement.quantityAfter      = quantityAfter;
        movement.comment            = comment;
        movement.createdAt          = createdAt;

        return movement;
    }
}