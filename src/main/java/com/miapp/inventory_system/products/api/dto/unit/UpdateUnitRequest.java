package com.miapp.inventory_system.products.api.dto.unit;

public record UpdateUnitRequest(
        String name,
        String symbol
) {}