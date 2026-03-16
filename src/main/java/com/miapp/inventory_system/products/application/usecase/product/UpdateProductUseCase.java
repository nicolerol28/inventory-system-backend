package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.application.command.product.UpdateProductCommand;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public Product execute(UpdateProductCommand command) {

        Product product = productRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con id: " + command.id()));

        if (productRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con el nombre: " + command.name());
        }

        if (productRepository.existsBySkuAndIdNot(command.sku(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con el SKU: " + command.sku());
        }

        product.update(
                command.name(),
                command.description(),
                command.sku(),
                command.unitId(),
                command.categoryId(),
                command.supplierId(),
                command.purchasePrice(),
                command.salePrice());

        return productRepository.save(product);
    }
}
