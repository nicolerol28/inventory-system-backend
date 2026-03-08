package com.miapp.inventory_system.products.domain.repository;

import com.miapp.inventory_system.products.domain.model.Unit;

import java.util.Optional;

public interface UnitRepository {

    Unit save(Unit unit);

    Optional<Unit> findById(Long id);

    boolean existsByName(String name);

    boolean existsBySymbol(String symbol);

    // Existe este nombre en otra Unit diferente a la mia?
    boolean existsByNameAndIdNot(String name, Long id);

    // Existe este simbolo en otra Unit diferente a la mia?
    boolean existsBySymbolAndIdNot(String symbol, Long id);
}