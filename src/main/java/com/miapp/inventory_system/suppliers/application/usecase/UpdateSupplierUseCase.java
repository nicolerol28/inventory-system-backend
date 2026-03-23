package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.suppliers.application.command.UpdateSupplierCommand;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public Supplier execute(UpdateSupplierCommand command) {

        Supplier supplier = supplierRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un proveedor con id: " + command.id()));

        if (supplierRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otro proveedor con el nombre: " + command.name());
        }

        // Solo validamos el teléfono si no es null, ya que es un campo opcional
        if (command.phone() != null &&
                supplierRepository.existsByPhoneAndIdNot(command.phone(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otro proveedor con el teléfono: " + command.phone());
        }

        supplier.update(
                command.name(),
                command.contact(),
                command.phone());

        return supplierRepository.save(supplier);
    }
}
