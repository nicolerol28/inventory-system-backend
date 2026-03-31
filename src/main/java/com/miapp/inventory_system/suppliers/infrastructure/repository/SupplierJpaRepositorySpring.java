package com.miapp.inventory_system.suppliers.infrastructure.repository;

import com.miapp.inventory_system.suppliers.infrastructure.entity.SupplierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierJpaRepositorySpring extends JpaRepository<SupplierJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("SELECT s FROM SupplierJpaEntity s WHERE s.active = true")
    List<SupplierJpaEntity> findAllActive();

    Page<SupplierJpaEntity> findByActiveTrue(Pageable pageable);

    Page<SupplierJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<SupplierJpaEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
