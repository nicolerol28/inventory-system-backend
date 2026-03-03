package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.InventoryMovementRepository;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
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

        // Obtener stock actual para el producto y almacen especificados
        Stock stock = stockRepository
                .findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró stock para el producto " + command.productId() +
                                " en el almacén " + command.warehouseId()));

        // El dominio crea el movimiento y aplica las reglas de negocio
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

        // El stock actualiza su cantidad con el resultado del movimiento
        stock.apply(movement);

        // Capturamos savedMovement porque el repositorio devuelve un objeto nuevo
        // con el ID asignado por postgresql, el objeto original permanece inmutable.
        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);
        stockRepository.save(stock);

        // Retornamos el objeto con su id de la base de datos
        return savedMovement;
    }
}