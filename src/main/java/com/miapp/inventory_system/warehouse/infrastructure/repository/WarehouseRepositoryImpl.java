package com.miapp.inventory_system.warehouse.infrastructure.repository;

import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
import com.miapp.inventory_system.warehouse.infrastructure.entity.WarehouseJpaEntity;
import com.miapp.inventory_system.warehouse.infrastructure.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final WarehouseJpaRepositorySpring jpaRepository;
    private final WarehouseMapper mapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity entity = mapper.toEntity(warehouse);
        WarehouseJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }
}
