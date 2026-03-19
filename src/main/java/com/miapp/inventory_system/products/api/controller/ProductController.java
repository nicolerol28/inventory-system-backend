package com.miapp.inventory_system.products.api.controller;

import com.miapp.inventory_system.products.api.dto.product.ProductResponse;
import com.miapp.inventory_system.products.api.dto.product.RegisterProductRequest;
import com.miapp.inventory_system.products.api.dto.product.UpdateProductRequest;
import com.miapp.inventory_system.products.api.mapper.ProductApiMapper;
import com.miapp.inventory_system.products.application.query.ProductQueryService;
import com.miapp.inventory_system.products.application.usecase.product.DeactivateProductUseCase;
import com.miapp.inventory_system.products.application.usecase.product.RegisterProductUseCase;
import com.miapp.inventory_system.products.application.usecase.product.UpdateProductUseCase;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductApiMapper mapper;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final RegisterProductUseCase registerProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;

    @PostMapping
    public ResponseEntity<ProductResponse> register(
            @RequestBody RegisterProductRequest request) {

        Product product = registerProductUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {

        Product product = updateProductUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(productQueryService.getAll(page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<ProductResponse>> getActive(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(productQueryService.getAllActive(page, size));
    }

}
