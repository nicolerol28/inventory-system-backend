package com.miapp.inventory_system.products.application.query;

import com.miapp.inventory_system.products.api.dto.unit.UnitResponse;
import com.miapp.inventory_system.products.infrastructure.entity.UnitJpaEntity;
import com.miapp.inventory_system.products.infrastructure.repository.UnitJpaRepositorySpring;
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
public class UnitQueryService {

    private final UnitJpaRepositorySpring jpaRepository;

    public UnitResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una unidad con id: " + id));
    }

    public PageResponse<UnitResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UnitJpaEntity> result = jpaRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public List<UnitResponse> getAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UnitResponse toResponse(UnitJpaEntity entity) {
        return new UnitResponse(
                entity.getId(),
                entity.getName(),
                entity.getSymbol(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private PageResponse<UnitResponse> toPageResponse(Page<UnitJpaEntity> page) {
        List<UnitResponse> content = page.getContent()
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