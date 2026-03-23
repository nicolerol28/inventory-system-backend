package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void execute(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un proveedor con id: " + id));

        if (productRepository.existsActiveBySupplierId(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar un proveedor con productos activos asociados");
        }

        supplier.deactivate();
        supplierRepository.save(supplier);
    }
}
