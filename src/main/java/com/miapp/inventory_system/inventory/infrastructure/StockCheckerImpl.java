package com.miapp.inventory_system.inventory.infrastructure;

import com.miapp.inventory_system.inventory.infrastructure.repository.StockJpaRepositorySpring;
import com.miapp.inventory_system.warehouse.domain.StockChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockCheckerImpl implements StockChecker {

    private final StockJpaRepositorySpring stockJpaRepository;

    @Override
    public boolean hasActiveStockByWarehouseId(Long warehouseId) {
        return stockJpaRepository.existsByWarehouseId(warehouseId);
    }
}