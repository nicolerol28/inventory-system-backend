package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.warehouse.application.command.UpdateWarehouseCommand;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public Warehouse execute(UpdateWarehouseCommand command) {

        Warehouse warehouse = warehouseRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un almacén con id: " + command.id()));

        if (warehouseRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otro almacén con el nombre: " + command.name());
        }

        warehouse.update(
                command.name(),
                command.location());

        return warehouseRepository.save(warehouse);
    }
}
