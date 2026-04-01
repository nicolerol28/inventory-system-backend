package com.miapp.inventory_system.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateMinQuantityRequest(
        @NotNull
        @PositiveOrZero
        BigDecimal minQuantity
) {}
