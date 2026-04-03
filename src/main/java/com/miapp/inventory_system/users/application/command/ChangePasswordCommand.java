package com.miapp.inventory_system.users.application.command;

public record ChangePasswordCommand(
        Long userId,
        String currentPassword,
        String newPassword
) {}
