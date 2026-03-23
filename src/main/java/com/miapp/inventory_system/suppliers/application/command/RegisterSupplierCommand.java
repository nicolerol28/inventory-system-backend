package com.miapp.inventory_system.suppliers.application.command;

public record RegisterSupplierCommand(
        String name,
        String contact,
        String phone
){}
