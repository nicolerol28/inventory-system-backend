package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.application.command.product.RegisterProductCommand;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public Product execute(RegisterProductCommand command) {

        if (productRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre: " + command.name());
        }

        if (productRepository.existsBySku(command.sku())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el SKU: " + command.sku());
        }

        Product product = Product.create(
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
