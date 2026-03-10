package com.miapp.inventory_system.products.application.query;

import com.miapp.inventory_system.products.api.dto.category.CategoryResponse;
import com.miapp.inventory_system.products.infrastructure.entity.CategoryJpaEntity;
import com.miapp.inventory_system.products.infrastructure.repository.CategoryJpaRepositorySpring;
import com.miapp.inventory_system.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryJpaRepositorySpring jpaRepository;

    public CategoryResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una categoria con el id: " + id));
    }

    public PageResponse<CategoryResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CategoryJpaEntity> result = jpaRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public List<CategoryResponse> getAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(CategoryJpaEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private PageResponse<CategoryResponse> toPageResponse(Page<CategoryJpaEntity> page) {
        List<CategoryResponse> content = page.getContent()
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
