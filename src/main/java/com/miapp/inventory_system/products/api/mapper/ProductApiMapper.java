package com.miapp.inventory_system.products.api.mapper;

import com.miapp.inventory_system.products.api.dto.product.ProductResponse;
import com.miapp.inventory_system.products.api.dto.product.RegisterProductRequest;
import com.miapp.inventory_system.products.api.dto.product.UpdateProductRequest;
import com.miapp.inventory_system.products.application.command.product.RegisterProductCommand;
import com.miapp.inventory_system.products.application.command.product.UpdateProductCommand;
import com.miapp.inventory_system.products.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductApiMapper {

    public RegisterProductCommand toCommand(RegisterProductRequest request) {
        return new RegisterProductCommand(
                request.name(),
                request.description(),
                request.sku(),
                request.unitId(),
                request.categoryId(),
                request.supplierId(),
                Optional.ofNullable(request.purchasePrice()),
                Optional.ofNullable(request.salePrice())
        );
    }

    public UpdateProductCommand toCommand(UpdateProductRequest request, Long id) {
        return new UpdateProductCommand(
                id,
                request.name(),
                request.description(),
                request.sku(),
                request.unitId(),
                request.categoryId(),
                request.supplierId(),
                Optional.ofNullable(request.purchasePrice()),
                Optional.ofNullable(request.salePrice())
        );
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getUnitId(),
                product.getCategoryId(),
                product.getSupplierId(),
                product.getPurchasePrice().orElse(null),
                product.getSalePrice().orElse(null),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
