package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.warehouse.application.command.RegisterWarehouseCommand;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public Warehouse execute(RegisterWarehouseCommand command) {

        if (warehouseRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Ya existe un almacen con el nombre: " + command.name());
        }

        Warehouse warehouse = Warehouse.create(
                command.name(),
                command.location());
        return warehouseRepository.save(warehouse);
    }
}
