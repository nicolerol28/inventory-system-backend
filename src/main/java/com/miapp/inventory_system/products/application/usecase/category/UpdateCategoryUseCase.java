package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.application.command.category.UpdateCategoryCommand;
import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category execute(UpdateCategoryCommand command){

        Category category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada con id: " + command.id()));

        if (categoryRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otra categoría con el mismo nombre" + command.name());
        }

        category.update(command.name());
        return categoryRepository.save(category);
    }
}
