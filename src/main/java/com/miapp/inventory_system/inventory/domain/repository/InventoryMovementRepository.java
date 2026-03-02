package com.miapp.inventory_system.inventory.domain.repository;

import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;

import java.util.Optional;

public interface InventoryMovementRepository {

    InventoryMovement save(InventoryMovement movement);

    Optional<InventoryMovement> findById(Long id);
}