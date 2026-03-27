package com.miapp.inventory_system.users.application.command;

import com.miapp.inventory_system.users.domain.model.Role;

public record RegisterUserCommand(
        String name,
        String email,
        String password,
        Role role
) {}