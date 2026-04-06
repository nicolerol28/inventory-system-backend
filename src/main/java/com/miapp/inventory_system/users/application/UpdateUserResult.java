package com.miapp.inventory_system.users.application;

import com.miapp.inventory_system.users.domain.model.Role;

public record UpdateUserResult(String token, Long userId, String name, String email, Role role, boolean active) {}
