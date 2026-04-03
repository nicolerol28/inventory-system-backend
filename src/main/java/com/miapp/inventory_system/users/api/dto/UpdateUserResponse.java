package com.miapp.inventory_system.users.api.dto;

import com.miapp.inventory_system.users.domain.model.Role;

public record UpdateUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        String token
) {}
