package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.model.dto.PasswordResetRequestedEvent;
import dev.ctrlspace.bootcamp_2025_03.model.dto.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Service
public class N8nNotificationService {

    static final String SECRET_HEADER = "X-Webhook-Secret";

    private final RestTemplate restTemplate;
    private final String webhookUrl;
    private final String webhookSecret;
    private final String frontendBaseUrl;

    @Autowired
    public N8nNotificationService(RestTemplateBuilder restTemplateBuilder,
                                  @Value("${N8N_WEBHOOK_URL:}") String webhookUrl,
                                  @Value("${N8N_WEBHOOK_SECRET:}") String webhookSecret,
                                  @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this(restTemplateBuilder
                        .connectTimeout(Duration.ofSeconds(5))
                        .readTimeout(Duration.ofSeconds(15))
                        .build(),
                webhookUrl, webhookSecret, frontendBaseUrl);
    }

    N8nNotificationService(RestTemplate restTemplate, String webhookUrl, String webhookSecret, String frontendBaseUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public boolean isEnabled() {
        return !webhookUrl.isBlank();
    }

    public void sendUserRegistered(String toEmail, String name, String token) throws IOException {
        UserRegisteredEvent event = new UserRegisteredEvent(
                toEmail, name, frontendBaseUrl + "/login?token=" + token);
        post(event, event.getType());
    }

    public void sendPasswordResetRequested(String toEmail, String name, String token) throws IOException {
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                toEmail, name, frontendBaseUrl + "/reset-password?token=" + token);
        post(event, event.getType());
    }

    private void post(Object event, String type) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!webhookSecret.isBlank()) {
            headers.set(SECRET_HEADER, webhookSecret);
        }

        try {
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(event, headers), String.class);
        } catch (RestClientException ex) {
            throw new IOException("Failed to send " + type + " event to n8n", ex);
        }
    }
}
