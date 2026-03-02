package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.RegisterStockEntryCommand;
import com.miapp.inventory_system.inventory.domain.exception.InsufficientStockException;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.InventoryMovementRepository;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterStockEntryUseCase {

    private final StockRepository stockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public InventoryMovement execute(RegisterStockEntryCommand command) {

        // Obtener el stock actual
        Stock stock = stockRepository
                .findByProductIdAndWarehouseId(
                        command.productId(),
                        command.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe stock registrado para este producto en este almacén"));

        // Crear el movimiento — aqui viven las reglas de negocio
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

        // Aplicar el movimiento al stock
        stock.apply(movement);

        // Persistir ambos en la misma transaccion atomica
        inventoryMovementRepository.save(movement);
        stockRepository.save(stock);

        return movement;
    }
}