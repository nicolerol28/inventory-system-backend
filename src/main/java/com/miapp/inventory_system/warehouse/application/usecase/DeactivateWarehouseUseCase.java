package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.warehouse.domain.StockChecker;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeactivateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;
    private final StockChecker stockChecker;

    @Transactional
    public void execute(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un almacén con id: " + id));

        if (stockChecker.hasActiveStockByWarehouseId(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar un almacén con stock activo asociado");
        }

        warehouse.deactivate();
        warehouseRepository.save(warehouse);
    }
}




