package com.miapp.inventory_system.suppliers.application.query;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.suppliers.api.dto.SupplierResponse;
import com.miapp.inventory_system.suppliers.infrastructure.entity.SupplierJpaEntity;
import com.miapp.inventory_system.suppliers.infrastructure.repository.SupplierJpaRepositorySpring;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierQueryService {

    private final SupplierJpaRepositorySpring jpaRepository;

    public SupplierResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un proveedor con el id: " + id));
    }

    public PageResponse<SupplierResponse> getAll(int page, int size, String name, String filterActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<SupplierJpaEntity> result;
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

    public List<SupplierResponse> getAllActive() {
        return jpaRepository.findAllActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SupplierResponse toResponse(SupplierJpaEntity entity) {
        return new SupplierResponse(
                entity.getId(),
                entity.getName(),
                entity.getContact(),
                entity.getPhone(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PageResponse<SupplierResponse> toPageResponse(Page<SupplierJpaEntity> page) {
        List<SupplierResponse> content = page.getContent()
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
