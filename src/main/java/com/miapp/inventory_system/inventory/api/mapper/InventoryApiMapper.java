package com.miapp.inventory_system.inventory.api.mapper;

import com.miapp.inventory_system.inventory.api.dto.InventoryMovementResponse;
import com.miapp.inventory_system.inventory.api.dto.RegisterStockMovementRequest;
import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import org.springframework.stereotype.Component;

@Component
public class InventoryApiMapper {

    public RegisterStockMovementCommand toCommand(RegisterStockMovementRequest request) {
        return new RegisterStockMovementCommand(
                request.productId(),
                request.warehouseId(),
                request.supplierId(),
                request.registeredBy(),
                request.movementType(),
                request.quantity(),
                request.comment()
        );
    }

    public InventoryMovementResponse toResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getWarehouseId(),
                movement.getSupplierId(),
                movement.getRegisteredBy(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getComment(),
                movement.getCreatedAt()
        );
    }
}