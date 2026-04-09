package com.miapp.inventory_system.shared.guard;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AssistantGuard {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    private static final List<String> INJECTION_PATTERNS = List.of(
            "ignore previous", "ignora las instrucciones", "ignora todo",
            "olvida las instrucciones", "system prompt", "jailbreak",
            "act as", "eres ahora", "pretend you", "pretend to be",
            "forget your instructions", "new instructions", "nuevas instrucciones",
            "override", "bypass", "DAN", "do anything now"
    );

    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    public String validate(String message, String clientIp) {
        String sanitized = sanitize(message);
        enforceRateLimit(clientIp);
        checkForInjection(sanitized);
        return sanitized;
    }

    private String sanitize(String message) {
        return message.trim().replaceAll("\\s+", " ");
    }

    private void enforceRateLimit(String clientIp) {
        System.out.println("[AssistantGuard] IP recibida: " + clientIp);
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60_000L;

        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < oneMinuteAgo) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                throw new IllegalStateException("Límite de solicitudes alcanzado. Espera un momento.");
            }
            timestamps.addLast(now);
        }
    }

    private void checkForInjection(String message) {
        String lower = message.toLowerCase();
        for (String pattern : INJECTION_PATTERNS) {
            if (lower.contains(pattern.toLowerCase())) {
                throw new IllegalArgumentException("El mensaje contiene contenido no permitido.");
            }
        }
    }
}
