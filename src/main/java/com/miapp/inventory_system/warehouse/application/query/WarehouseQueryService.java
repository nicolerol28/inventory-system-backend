package com.miapp.inventory_system.warehouse.application.query;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.warehouse.api.dto.WarehouseResponse;
import com.miapp.inventory_system.warehouse.infrastructure.entity.WarehouseJpaEntity;
import com.miapp.inventory_system.warehouse.infrastructure.repository.WarehouseJpaRepositorySpring;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseQueryService {

    private final WarehouseJpaRepositorySpring jpaRepository;

    public WarehouseResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un almacen con el id: " + id));
    }

    public PageResponse<WarehouseResponse> getAll(int page, int size, String name, String filterActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<WarehouseJpaEntity> result;
        boolean hasName = name != null && !name.isBlank();
        boolean onlyActive = "active".equals(filterActive);

        if (hasName && onlyActive) {
            result = jpaRepository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable);
        } else if (hasName) {
            result = jpaRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (onlyActive) {
            result = jpaRepository.findByActiveTrue(pageable);
        } else {
            result = jpaRepository.findAll(pageable);
        }

        return toPageResponse(result);
    }

    public List<WarehouseResponse> getAllActive() {
        return jpaRepository.findAllActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private WarehouseResponse toResponse(WarehouseJpaEntity entity) {
        return new WarehouseResponse(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private PageResponse<WarehouseResponse> toPageResponse(Page<WarehouseJpaEntity> page) {
        List<WarehouseResponse> content = page.getContent()
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