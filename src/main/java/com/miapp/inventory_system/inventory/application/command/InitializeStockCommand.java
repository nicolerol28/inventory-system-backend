package com.miapp.inventory_system.inventory.application.command;

import java.math.BigDecimal;

public record InitializeStockCommand(
        Long productId,
        Long warehouseId,
        BigDecimal minQuantity
) {}