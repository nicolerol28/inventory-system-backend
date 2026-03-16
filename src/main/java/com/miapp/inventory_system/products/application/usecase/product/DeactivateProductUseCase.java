package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con id: " + id));

        product.deactivate();
        productRepository.save(product);
    }
}
