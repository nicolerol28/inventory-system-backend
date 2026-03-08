package com.miapp.inventory_system.products.application.command.unit;

public record UpdateUnitCommand(
        Long id,
        String name,
        String symbol
) {}