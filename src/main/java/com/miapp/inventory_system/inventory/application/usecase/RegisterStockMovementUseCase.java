package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.InventoryMovementRepository;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterStockMovementUseCase {

    private final StockRepository stockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public InventoryMovement execute(RegisterStockMovementCommand command) {

        Stock stock = stockRepository
                .findByProductIdAndWarehouseId(
                        command.productId(),
                        command.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe stock registrado para este producto en este almacén"));

        InventoryMovement movement = InventoryMovement.create(
                command.productId(),
                command.warehouseId(),
                command.supplierId(),
                command.registeredBy(),
                command.movementType(),
                command.quantity(),
                stock.getQuantity(),
                command.comment()
        );

        stock.apply(movement);

        inventoryMovementRepository.save(movement);
        stockRepository.save(stock);

        return movement;
    }
}