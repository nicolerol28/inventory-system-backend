package com.miapp.inventory_system.products.application.command.unit;

public record RegisterUnitCommand(
        String name,
        String symbol
) {}