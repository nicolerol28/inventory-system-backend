package com.miapp.inventory_system.inventory.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Stock {

    private Long id;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private BigDecimal minQuantity;
    private LocalDateTime updatedAt;

    private Stock() {}

    // Stock por primera vez
    public static Stock create(
            Long productId,
            Long warehouseId,
            BigDecimal minQuantity) {

        Stock stock = new Stock();
        stock.productId   = productId;
        stock.warehouseId = warehouseId;
        stock.quantity    = BigDecimal.ZERO;
        stock.minQuantity = minQuantity;
        stock.updatedAt   = LocalDateTime.now();

        return stock;
    }

    public void apply(InventoryMovement movement) {
        if (!this.productId.equals(movement.getProductId()) ||
                !this.warehouseId.equals(movement.getWarehouseId())) {
            throw new IllegalArgumentException(
                    "El movimiento no corresponde a este stock");
        }

        this.quantity  = movement.getQuantityAfter();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMinQuantity(BigDecimal minQuantity) {
        if (minQuantity == null || minQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad mínima no puede ser negativa");
        }
        this.minQuantity = minQuantity;
        this.updatedAt   = LocalDateTime.now();
    }

    public boolean isBelowMinimum() {
        return this.quantity.compareTo(this.minQuantity) < 0;
    }

    // Reconstituir un Stock desde la base de datos
    public static Stock reconstitute(
            Long id,
            Long productId,
            Long warehouseId,
            BigDecimal quantity,
            BigDecimal minQuantity,
            LocalDateTime updatedAt) {

        Stock stock = new Stock();
        stock.id          = id;
        stock.productId   = productId;
        stock.warehouseId = warehouseId;
        stock.quantity    = quantity;
        stock.minQuantity = minQuantity;
        stock.updatedAt   = updatedAt;

        return stock;
    }
}












