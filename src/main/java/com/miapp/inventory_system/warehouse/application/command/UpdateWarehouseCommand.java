package com.miapp.inventory_system.warehouse.application.command;

public record UpdateWarehouseCommand(
        Long id,
        String name,
        String location
){}
