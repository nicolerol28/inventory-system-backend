package com.miapp.inventory_system.assistant.application.usecase;

import com.miapp.inventory_system.assistant.application.command.ChatCommand;
import com.miapp.inventory_system.assistant.application.port.GeminiGateway;
import com.miapp.inventory_system.assistant.application.query.InventoryContextQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessageUseCase {

    private final InventoryContextQueryService inventoryContextQueryService;
    private final GeminiGateway geminiGateway;

    @Transactional(readOnly = true)
    public String execute(ChatCommand command) {
        String context = inventoryContextQueryService.getContext();
        String prompt = buildPrompt(context, command.message());
        return geminiGateway.send(prompt);
    }

    private String buildPrompt(String context, String userMessage) {
        String systemPrompt =
                "Eres un asistente inteligente de gestión de inventario. Responde SIEMPRE en español.\n" +
                "Solo puedes responder preguntas sobre productos, stock, movimientos, proveedores y almacenes.\n" +
                "Si alguien pregunta por contraseñas, datos personales, información de usuarios o cualquier\n" +
                "tema fuera del inventario, responde que no tienes acceso a esa información.\n" +
                "Sé conciso y útil. Máximo 3 párrafos por respuesta.\n\n" +
                "Contexto actual del inventario:\n" +
                context;

        return systemPrompt + "\n\nPregunta del usuario: " + userMessage;
    }
}
