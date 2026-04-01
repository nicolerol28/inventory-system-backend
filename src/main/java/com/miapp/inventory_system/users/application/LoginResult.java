package com.miapp.inventory_system.users.application;

import java.time.LocalDateTime;

public record LoginResult(
        String token,
        Long userId,
        String role,
        LocalDateTime expiresAt
) {}
