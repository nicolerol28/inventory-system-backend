package com.miapp.inventory_system.inventory.domain.repository;

import com.miapp.inventory_system.inventory.domain.model.Stock;

import java.util.Optional;

public interface StockRepository {

    Optional<Stock> findById(Long id);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    Stock save(Stock stock);
}