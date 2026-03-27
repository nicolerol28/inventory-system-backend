package com.miapp.inventory_system.users.api.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        Long userId,
        String role,
        LocalDateTime expiresAt
) {}