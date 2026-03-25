package com.miapp.inventory_system.warehouse.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Warehouse {
    private Long id;
    private String name;
    private String location;
    private boolean active;
    private LocalDateTime createdAt;

     private Warehouse() {}

    public static Warehouse create(
            String name,
            String location) {

        validate(name);

        Warehouse warehouse = new Warehouse();
        warehouse.name      = name;
        warehouse.location  = location;
        warehouse.active    = true;
        warehouse.createdAt = LocalDateTime.now();

        return warehouse;
    }

    public static Warehouse reconstitute(
            Long id,
            String name,
            String location,
            boolean active,
            LocalDateTime createdAt) {

        Warehouse warehouse = new Warehouse();
        warehouse.id        = id;
        warehouse.name      = name;
        warehouse.location  = location;
        warehouse.active    = active;
        warehouse.createdAt = createdAt;

        return warehouse;
    }

    public void update(
            String name,
            String location) {

        validate(name);

        this.name     = name;
        this.location = location;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "El almacen ya está desactivado");
        }
        this.active = false;
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del almacen es obligatorio");
        }
    }
}
