package com.miapp.inventory_system.suppliers.application.command;

public record UpdateSupplierCommand(
        Long id,
        String name,
        String contact,
        String phone
) {}
