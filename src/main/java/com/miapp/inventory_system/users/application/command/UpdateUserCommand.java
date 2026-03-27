package com.miapp.inventory_system.users.application.command;

import com.miapp.inventory_system.users.domain.model.Role;

public record UpdateUserCommand(
        Long id,
        String name,
        String email,
        Role role
) {}