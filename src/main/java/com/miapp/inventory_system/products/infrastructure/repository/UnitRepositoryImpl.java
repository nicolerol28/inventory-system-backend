package com.miapp.inventory_system.products.infrastructure.repository;

import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import com.miapp.inventory_system.products.infrastructure.mapper.UnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UnitRepositoryImpl implements UnitRepository {

    private final UnitJpaRepositorySpring jpaRepository;
    private final UnitMapper mapper;

    @Override
    public Unit save(Unit unit) {
        UnitJpaEntity entity = mapper.toEntity(unit);
        UnitJpaEntity saved  = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Unit> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsBySymbol(String symbol) {
        return jpaRepository.existsBySymbol(symbol);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public boolean existsBySymbolAndIdNot(String symbol, Long id) {
        return jpaRepository.existsBySymbolAndIdNot(symbol, id);
    }
}