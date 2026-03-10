package com.miapp.inventory_system.products.api.mapper;

import com.miapp.inventory_system.products.api.dto.category.CategoryResponse;
import com.miapp.inventory_system.products.api.dto.category.RegisterCategoryRequest;
import com.miapp.inventory_system.products.api.dto.category.UpdateCategoryRequest;
import com.miapp.inventory_system.products.application.command.category.RegisterCategoryCommand;
import com.miapp.inventory_system.products.application.command.category.UpdateCategoryCommand;
import com.miapp.inventory_system.products.domain.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryApiMapper {

    public RegisterCategoryCommand toCommand(RegisterCategoryRequest request){
        return new RegisterCategoryCommand(
                request.name()
        );
    }

    public UpdateCategoryCommand toCommand(UpdateCategoryRequest request, Long id){
        return new UpdateCategoryCommand(
                id,
                request.name()
        );
    }

    public CategoryResponse toResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
