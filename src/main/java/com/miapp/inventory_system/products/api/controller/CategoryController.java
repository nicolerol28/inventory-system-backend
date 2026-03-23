package com.miapp.inventory_system.products.api.controller;

import com.miapp.inventory_system.products.api.dto.category.CategoryResponse;
import com.miapp.inventory_system.products.api.dto.category.RegisterCategoryRequest;
import com.miapp.inventory_system.products.api.dto.category.UpdateCategoryRequest;
import com.miapp.inventory_system.products.api.mapper.CategoryApiMapper;
import com.miapp.inventory_system.products.application.query.CategoryQueryService;
import com.miapp.inventory_system.products.application.usecase.category.DeactivateCategoryUseCase;
import com.miapp.inventory_system.products.application.usecase.category.RegisterCategoryUseCase;
import com.miapp.inventory_system.products.application.usecase.category.UpdateCategoryUseCase;
import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final RegisterCategoryUseCase registerCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final CategoryQueryService categoryQueryService;
    private final CategoryApiMapper mapper;

    @PostMapping
    public ResponseEntity<CategoryResponse> register(
            @Valid @RequestBody RegisterCategoryRequest request) {

        Category category = registerCategoryUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        Category category = updateCategoryUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateCategoryUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(categoryQueryService.getAll(page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getAllActive() {
        return ResponseEntity.ok(categoryQueryService.getAllActive());
    }

}
