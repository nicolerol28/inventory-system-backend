package com.miapp.inventory_system.assistant.application.command;

public record ChatCommand(
        String message,
        String clientIp
) {}
