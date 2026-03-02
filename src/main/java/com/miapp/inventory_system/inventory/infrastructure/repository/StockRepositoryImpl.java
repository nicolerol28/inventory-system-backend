package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import com.miapp.inventory_system.inventory.infrastructure.entity.StockJpaEntity;
import com.miapp.inventory_system.inventory.infrastructure.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepositorySpring jpaRepository;
    private final StockMapper mapper;

    @Override
    public Optional<Stock> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId) {

        return jpaRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .map(mapper::toDomain);
    }

    @Override
    public Stock save(Stock stock) {
        StockJpaEntity entity = mapper.toEntity(stock);
        StockJpaEntity saved  = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}