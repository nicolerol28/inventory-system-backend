package com.miapp.inventory_system.suppliers.infrastructure.repository;

import com.miapp.inventory_system.suppliers.infrastructure.entity.SupplierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierJpaRepositorySpring extends JpaRepository<SupplierJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    List<SupplierJpaEntity> findByActiveTrue();
}
