package com.miapp.inventory_system.products.application.command.category;

public record UpdateCategoryCommand(
        Long id,
        String name
) {}
