package com.miapp.inventory_system.users.api.dto;

import com.miapp.inventory_system.users.domain.model.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}