package com.miapp.inventory_system.assistant.api.controller;

import com.miapp.inventory_system.assistant.api.dto.ChatRequest;
import com.miapp.inventory_system.assistant.api.dto.ChatResponse;
import com.miapp.inventory_system.assistant.api.mapper.AssistantApiMapper;
import com.miapp.inventory_system.assistant.application.usecase.SendChatMessageUseCase;
import com.miapp.inventory_system.shared.guard.AssistantGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final AssistantApiMapper mapper;
    private final AssistantGuard guard;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        String sanitized = guard.validate(request.message(), clientIp);
        String reply = sendChatMessageUseCase.execute(mapper.toCommand(sanitized, clientIp));
        return ResponseEntity.ok(mapper.toResponse(reply));
    }
}
