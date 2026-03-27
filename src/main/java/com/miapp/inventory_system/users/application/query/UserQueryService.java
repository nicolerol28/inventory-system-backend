package com.miapp.inventory_system.users.application.query;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.domain.model.Role;
import com.miapp.inventory_system.users.infrastructure.entity.UserJpaEntity;
import com.miapp.inventory_system.users.infrastructure.repository.UserJpaRepositorySpring;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserJpaRepositorySpring jpaRepository;

    public UserResponse getById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un usuario con el id: " + id));
    }

    public PageResponse<UserResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UserJpaEntity> result = jpaRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public List<UserResponse> getAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(UserJpaEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                Role.valueOf(entity.getRole()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PageResponse<UserResponse> toPageResponse(Page<UserJpaEntity> page) {
        List<UserResponse> content = page.getContent()
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