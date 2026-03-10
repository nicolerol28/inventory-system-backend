package com.miapp.inventory_system.products.api.mapper;

import com.miapp.inventory_system.products.api.dto.unit.RegisterUnitRequest;
import com.miapp.inventory_system.products.api.dto.unit.UnitResponse;
import com.miapp.inventory_system.products.api.dto.unit.UpdateUnitRequest;
import com.miapp.inventory_system.products.application.command.unit.RegisterUnitCommand;
import com.miapp.inventory_system.products.application.command.unit.UpdateUnitCommand;
import com.miapp.inventory_system.products.domain.model.Unit;
import org.springframework.stereotype.Component;

@Component
public class UnitApiMapper {

    public RegisterUnitCommand toCommand(RegisterUnitRequest request) {
        return new RegisterUnitCommand(
                request.name(),
                request.symbol()
        );
    }

    public UpdateUnitCommand toCommand(UpdateUnitRequest request, Long id) {
        return new UpdateUnitCommand(
                id,
                request.name(),
                request.symbol()
        );
    }

    public UnitResponse toResponse(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getSymbol(),
                unit.isActive(),
                unit.getCreatedAt()
        );
    }
}