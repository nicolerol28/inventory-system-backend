package com.miapp.inventory_system.shared.infrastructure.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class AiWebhookNotifier {

    private final String webhookUrl;
    private final String webhookSecret;
    private final HttpClient httpClient;

    public AiWebhookNotifier(
            @Value("${ai.webhook.url:}") String webhookUrl,
            @Value("${ai.webhook.secret:}") String webhookSecret) {
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void notifyProductCreated(Long productId) {
        sendEvent("PRODUCT_CREATED", productId);
    }

    public void notifyProductUpdated(Long productId) {
        sendEvent("PRODUCT_UPDATED", productId);
    }

    public void notifyProductDeleted(Long productId) {
        sendEvent("PRODUCT_DELETED", productId);
    }

    private void sendEvent(String event, Long productId) {
        if (webhookUrl.isBlank()) {
            log.debug("AI webhook URL not configured, skipping notification");
            return;
        }

        String body = String.format("{\"event\":\"%s\",\"productId\":%d}", event, productId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .header("X-Webhook-Secret", webhookSecret)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build();

        // Async with retry — don't block the main transaction
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() >= 200 && res.statusCode() < 300) {
                        log.info("AI webhook sent: {} for product {}", event, productId);
                    } else {
                        log.warn("AI webhook failed ({}): {} for product {}", res.statusCode(), event, productId);
                        retry(request, event, productId, 1);
                    }
                })
                .exceptionally(ex -> {
                    log.warn("AI webhook error: {} for product {} — {}", event, productId, ex.getMessage());
                    retry(request, event, productId, 1);
                    return null;
                });
    }

    private void retry(HttpRequest request, String event, Long productId, int attempt) {
        if (attempt > 2) {
            log.error("AI webhook gave up after 3 attempts: {} for product {}", event, productId);
            return;
        }

        try {
            Thread.sleep(1000L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() >= 200 && res.statusCode() < 300) {
                        log.info("AI webhook retry {} succeeded: {} for product {}", attempt, event, productId);
                    } else {
                        log.warn("AI webhook retry {} failed ({}): {} for product {}", attempt, res.statusCode(), event, productId);
                        retry(request, event, productId, attempt + 1);
                    }
                })
                .exceptionally(ex -> {
                    log.warn("AI webhook retry {} error: {} for product {} — {}", attempt, event, productId, ex.getMessage());
                    retry(request, event, productId, attempt + 1);
                    return null;
                });
    }
}