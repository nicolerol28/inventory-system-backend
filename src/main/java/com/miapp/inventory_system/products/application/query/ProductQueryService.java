package com.miapp.inventory_system.products.application.query;

import com.miapp.inventory_system.products.api.dto.product.ProductResponse;
import com.miapp.inventory_system.products.infrastructure.entity.ProductJpaEntity;
import com.miapp.inventory_system.products.infrastructure.repository.ProductJpaRepositorySpring;
import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductJpaRepositorySpring jpaRepository;

    public ProductResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con id: " + id));
    }

    public PageResponse<ProductResponse> getAll(
            int page, int size, String name, Long categoryId,
            Long unitId, String sortName, String filterActive) {

        Sort sort = "desc".equals(sortName)
                ? Sort.by("name").descending()
                : Sort.by("name").ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        boolean onlyActive = "active".equals(filterActive);

        Page<ProductJpaEntity> result = onlyActive
                ? jpaRepository.findByActiveTrueAndFilters(name, categoryId, unitId, pageable)
                : jpaRepository.findByFilters(name, categoryId, unitId, pageable);

        return toPageResponse(result);
    }

    public PageResponse<ProductResponse> getAllActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductJpaEntity> result = jpaRepository.findByActiveTrue(pageable);
        return toPageResponse(result);
    }

    private ProductResponse toResponse(ProductJpaEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                entity.getUnitId(),
                entity.getCategoryId(),
                entity.getSupplierId(),
                entity.getPurchasePrice(),
                entity.getSalePrice(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PageResponse<ProductResponse> toPageResponse(Page<ProductJpaEntity> page) {
        List<ProductResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}