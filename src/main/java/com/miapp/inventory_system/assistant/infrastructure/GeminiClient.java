package com.miapp.inventory_system.assistant.infrastructure;

import com.miapp.inventory_system.assistant.application.port.GeminiGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient implements GeminiGateway {

    private final RestTemplate restTemplate;
    private final String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public GeminiClient(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String send(String prompt) {
        String url = GEMINI_URL + apiKey;

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(content));

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response == null) {
                return "Lo siento, no pude procesar tu pregunta. Intenta de nuevo.";
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> responseContent = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) responseContent.get("parts");
            String text = (String) parts.get(0).get("text");

            Map<String, Object> usageMetadata = (Map<String, Object>) response.get("usageMetadata");
            if (usageMetadata != null) {
                System.out.println("[Gemini] tokens — prompt: " + usageMetadata.get("promptTokenCount")
                        + " | respuesta: " + usageMetadata.get("candidatesTokenCount")
                        + " | total: " + usageMetadata.get("totalTokenCount"));
            }

            return text;

        } catch (Exception e) {
            System.err.println("Error llamando a Gemini: " + e.getMessage());
            e.printStackTrace();
            return "Lo siento, no pude procesar tu pregunta. Intenta de nuevo.";
        }
    }
}