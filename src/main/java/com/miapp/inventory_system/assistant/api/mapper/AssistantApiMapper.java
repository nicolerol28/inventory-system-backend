package com.miapp.inventory_system.assistant.api.mapper;

import com.miapp.inventory_system.assistant.api.dto.ChatResponse;
import com.miapp.inventory_system.assistant.application.command.ChatCommand;
import org.springframework.stereotype.Component;

@Component
public class AssistantApiMapper {

    public ChatCommand toCommand(String sanitizedMessage, String clientIp) {
        return new ChatCommand(sanitizedMessage, clientIp);
    }

    public ChatResponse toResponse(String reply) {
        return new ChatResponse(reply);
    }
}
