package com.miapp.inventory_system.suppliers.api.mapper;

import com.miapp.inventory_system.suppliers.api.dto.RegisterSupplierRequest;
import com.miapp.inventory_system.suppliers.api.dto.SupplierResponse;
import com.miapp.inventory_system.suppliers.api.dto.UpdateSupplierRequest;
import com.miapp.inventory_system.suppliers.application.command.RegisterSupplierCommand;
import com.miapp.inventory_system.suppliers.application.command.UpdateSupplierCommand;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierApiMapper {

    public RegisterSupplierCommand toCommand(RegisterSupplierRequest request) {
        return new RegisterSupplierCommand(
            request.name(),
            request.contact(),
            request.phone()
        );
    }

    public UpdateSupplierCommand toCommand(UpdateSupplierRequest request, Long id) {
        return new UpdateSupplierCommand(
            id,
            request.name(),
            request.contact(),
            request.phone()
        );
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
            supplier.getId(),
            supplier.getName(),
            supplier.getContact(),
            supplier.getPhone(),
            supplier.isActive(),
            supplier.getCreatedAt(),
            supplier.getUpdatedAt()
        );
    }
}
