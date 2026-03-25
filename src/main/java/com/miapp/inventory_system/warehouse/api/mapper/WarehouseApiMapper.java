package com.miapp.inventory_system.warehouse.api.mapper;

import com.miapp.inventory_system.warehouse.api.dto.RegisterWarehouseRequest;
import com.miapp.inventory_system.warehouse.api.dto.UpdateWarehouseRequest;
import com.miapp.inventory_system.warehouse.api.dto.WarehouseResponse;
import com.miapp.inventory_system.warehouse.application.command.RegisterWarehouseCommand;
import com.miapp.inventory_system.warehouse.application.command.UpdateWarehouseCommand;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseApiMapper {

    public RegisterWarehouseCommand toCommand(RegisterWarehouseRequest request) {
        return new RegisterWarehouseCommand(
            request.name(),
            request.location()
        );
    }

    public UpdateWarehouseCommand toCommand(UpdateWarehouseRequest request, Long id) {
        return new UpdateWarehouseCommand(
            id,
            request.name(),
            request.location()
        );
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
            warehouse.getId(),
            warehouse.getName(),
            warehouse.getLocation(),
            warehouse.isActive(),
            warehouse.getCreatedAt()
        );
    }
}
