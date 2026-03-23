package com.miapp.inventory_system.suppliers.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSupplierRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        String contact,
        String phone
) {}