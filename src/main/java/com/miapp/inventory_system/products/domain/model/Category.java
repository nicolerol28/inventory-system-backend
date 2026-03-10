package com.miapp.inventory_system.products.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class Category {

    private Long id;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;

    private Category(){}

    public static Category create(String name) {
        validate(name);

        Category category = new Category();
        category.name      = name;
        category.active = true;
        category.createdAt = LocalDateTime.now();

        return category;
    }

    public static Category reconstitute(
            Long id,
            String name,
            boolean active,
            LocalDateTime createdAt) {

        Category category = new Category();
        category.id        = id;
        category.name      = name;
        category.active    = active;
        category.createdAt = createdAt;

        return category;
    }

    public void update(String name) {
        validate(name);
        this.name = name;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "La categoría ya está desactivada");
        }
        this.active = false;
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría no puede ser nulo o vacío");
        }
    }
}
