package com.miapp.inventory_system.suppliers.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Supplier {

    private Long id;
    private String name;
    private String contact;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Supplier() {}

    public static Supplier create(
            String name,
            String contact,
            String phone) {

        validate(name);

        Supplier supplier = new Supplier();
        supplier.name      = name;
        supplier.contact   = contact;
        supplier.phone     = phone;
        supplier.active    = true;
        supplier.createdAt = LocalDateTime.now();
        supplier.updatedAt = LocalDateTime.now();

        return supplier;
    }

    public static Supplier reconstitute(
            Long id,
            String name,
            String contact,
            String phone,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        Supplier supplier = new Supplier();
        supplier.id        = id;
        supplier.name      = name;
        supplier.contact   = contact;
        supplier.phone     = phone;
        supplier.active    = active;
        supplier.createdAt = createdAt;
        supplier.updatedAt = updatedAt;

        return supplier;
    }

    public void update(
            String name,
            String contact,
            String phone) {

        validate(name);

        this.name      = name;
        this.contact   = contact;
        this.phone     = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "El proveedor ya está desactivado");
        }
        this.active = false;
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del proveedor es obligatorio");
        }
    }
}
