package com.miapp.inventory_system.warehouse.application.command;

public record RegisterWarehouseCommand(
        String name,
        String location
){}
