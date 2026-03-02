package com.miapp.inventory_system.inventory.infrastructure.repository;

import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.repository.InventoryMovementRepository;
import com.miapp.inventory_system.inventory.infrastructure.entity.InventoryMovementJpaEntity;
import com.miapp.inventory_system.inventory.infrastructure.mapper.InventoryMovementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryMovementRepositoryImpl implements InventoryMovementRepository {

    private final InventoryMovementJpaRepositorySpring jpaRepository;
    private final InventoryMovementMapper mapper;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovementJpaEntity entity  = mapper.toEntity(movement);
        InventoryMovementJpaEntity saved   = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InventoryMovement> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}