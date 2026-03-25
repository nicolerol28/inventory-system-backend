package com.miapp.inventory_system.warehouse.domain.repository;

import com.miapp.inventory_system.warehouse.domain.model.Warehouse;

import java.util.Optional;

public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}