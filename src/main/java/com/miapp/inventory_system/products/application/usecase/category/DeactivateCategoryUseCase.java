package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeactivateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void execute(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una categoria con id: " + id));

        category.deactivate();
        categoryRepository.save(category);
    }
}
