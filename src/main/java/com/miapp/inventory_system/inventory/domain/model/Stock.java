package com.miapp.inventory_system.inventory.domain.model;

import com.miapp.inventory_system.inventory.domain.exception.InsufficientStockException;

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

    public boolean isBelowMinimum() {
        return this.quantity.compareTo(this.minQuantity) < 0;
    }
}












