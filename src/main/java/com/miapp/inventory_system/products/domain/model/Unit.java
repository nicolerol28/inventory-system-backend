package com.miapp.inventory_system.products.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class Unit {

    private Long id;
    private String name;
    private String symbol;
    private boolean active;
    private LocalDateTime createdAt;

    private Unit() {}

    public static Unit create(String name, String symbol) {
        validate(name, symbol);

        Unit unit = new Unit();
        unit.name      = name;
        unit.symbol    = symbol;
        unit.active    = true;
        unit.createdAt = LocalDateTime.now();

        return unit;
    }

    public static Unit reconstitute(
            Long id,
            String name,
            String symbol,
            boolean active,
            LocalDateTime createdAt) {

        Unit unit = new Unit();
        unit.id        = id;
        unit.name      = name;
        unit.symbol    = symbol;
        unit.active    = active;
        unit.createdAt = createdAt;

        return unit;
    }

    public void update(String name, String symbol) {
        validate(name, symbol);
        this.name   = name;
        this.symbol = symbol;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "La unidad ya está desactivada");
        }
        this.active = false;
    }

    private static void validate(String name, String symbol) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la unidad no puede estar vacío");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "El símbolo de la unidad no puede estar vacío");
        }
    }
}