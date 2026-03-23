package com.miapp.inventory_system.suppliers.domain.repository;

import com.miapp.inventory_system.suppliers.domain.model.Supplier;

import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);

    Optional<Supplier> findById(Long id);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
