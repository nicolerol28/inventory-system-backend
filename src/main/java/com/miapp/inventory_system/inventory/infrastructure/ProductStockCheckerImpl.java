package com.miapp.inventory_system.inventory.infrastructure;

import com.miapp.inventory_system.inventory.infrastructure.repository.StockJpaRepositorySpring;
import com.miapp.inventory_system.products.domain.ProductStockChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductStockCheckerImpl implements ProductStockChecker {

    private final StockJpaRepositorySpring stockJpaRepository;

    @Override
    public boolean hasActiveStockByProductId(Long productId) {
        return stockJpaRepository.existsByProductId(productId);
    }
}
