package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.domain.ProductStockChecker;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductStockChecker productStockChecker;

    @Transactional
    public void execute(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con id: " + id));

        if (productStockChecker.hasActiveStockByProductId(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar un producto con stock activo asociado");
        }

        product.deactivate();
        productRepository.save(product);
    }
}
