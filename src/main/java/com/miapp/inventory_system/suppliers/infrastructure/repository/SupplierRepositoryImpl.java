package com.miapp.inventory_system.suppliers.infrastructure.repository; // Ubicación correcta

import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
import com.miapp.inventory_system.suppliers.infrastructure.entity.SupplierJpaEntity;
import com.miapp.inventory_system.suppliers.infrastructure.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryImpl implements SupplierRepository {

    private final SupplierJpaRepositorySpring jpaRepository;
    private final SupplierMapper mapper;

    @Override
    public Supplier save(Supplier supplier) {
        SupplierJpaEntity entity = mapper.toEntity(supplier);
        SupplierJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public boolean existsByPhoneAndIdNot(String phone, Long id) {
        return jpaRepository.existsByPhoneAndIdNot(phone, id);
    }
}