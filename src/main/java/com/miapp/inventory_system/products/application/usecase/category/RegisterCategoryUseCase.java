package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.application.command.category.RegisterCategoryCommand;
import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category execute(RegisterCategoryCommand command){

        if (categoryRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el mismo nombre");
        }

        Category category = Category.create(command.name());
        return categoryRepository.save(category);
    }
}
