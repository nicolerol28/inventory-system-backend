package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.suppliers.application.command.RegisterSupplierCommand;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterSupplierUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public Supplier execute(RegisterSupplierCommand command) {

        if (supplierRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Ya existe un proveedor con el nombre: " + command.name());
        }

        // Solo validamos el teléfono si no es null, ya que es un campo opcional
        if (command.phone() != null
                && supplierRepository.existsByPhone(command.phone())) {
            throw new IllegalArgumentException(
                    "Ya existe un proveedor con el teléfono: " + command.phone());
        }

        Supplier supplier = Supplier.create(
                command.name(),
                command.contact(),
                command.phone());
        return supplierRepository.save(supplier);
    }
}
