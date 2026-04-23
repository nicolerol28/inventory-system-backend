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
import com.miapp.inventory_system.shared.gateway.StorageGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final ProductQueryService productQueryService;
    private final ProductApiMapper mapper;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final RegisterProductUseCase registerProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final StorageGateway storageGateway;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> register(
            @RequestPart("data") @Valid RegisterProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        String imageUrl = uploadImageIfPresent(image);
        Product product = registerProductUseCase.execute(
                imageUrl != null ? mapper.toCommand(request, imageUrl) : mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(product));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        String imageUrl = uploadImageIfPresent(image);
        Product product = updateProductUseCase.execute(
                imageUrl != null ? mapper.toCommand(request, id, imageUrl) : mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    private String uploadImageIfPresent(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de imagen no permitido. Se aceptan: image/jpeg, image/png, image/webp");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }
        String original = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image";
        String fileName = "products/" + UUID.randomUUID() + "_" + sanitizeFilename(original);
        try {
            return storageGateway.uploadFile(fileName, image.getBytes(), contentType);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String normalized = original.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String name = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            return UUID.randomUUID().toString();
        }
        return name;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(defaultValue = "asc") String sortName,
            @RequestParam(defaultValue = "all") String filterActive) {

        return ResponseEntity.ok(productQueryService.getAll(page, size, name, categoryId, unitId, sortName, filterActive));
    }

    @GetMapping("/active")
    public ResponseEntity<PageResponse<ProductResponse>> getActive(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(productQueryService.getAllActive(page, size));
    }

}
