package dev.ctrlspace.bootcamp_2025_03.services;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class N8nNotificationServiceTest {

    private static final String WEBHOOK_URL = "https://example.app.n8n.cloud/webhook/user-registered";
    private static final String FRONTEND_URL = "http://localhost:3000";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    @Test
    void isEnabledOnlyWhenWebhookUrlIsSet() {
        assertTrue(new N8nNotificationService(restTemplate, WEBHOOK_URL, "", FRONTEND_URL).isEnabled());
        assertFalse(new N8nNotificationService(restTemplate, "", "", FRONTEND_URL).isEnabled());
    }

    @Test
    void sendUserRegisteredPostsEventWithVerificationLinkAndSecret() throws IOException {
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(N8nNotificationService.SECRET_HEADER, "s3cret"))
                .andExpect(content().json("""
                        {"type": "user.registered",
                         "email": "new.user@example.com",
                         "name": "New User",
                         "verificationLink": "http://localhost:3000/login?token=abc-123"}
                        """, true))
                .andRespond(withSuccess("{\"message\":\"Workflow was started\"}", MediaType.APPLICATION_JSON));

        new N8nNotificationService(restTemplate, WEBHOOK_URL, "s3cret", FRONTEND_URL)
                .sendUserRegistered("new.user@example.com", "New User", "abc-123");

        server.verify();
    }

    @Test
    void sendPasswordResetRequestedPostsEventWithResetLinkAndNoSecretWhenUnset() throws IOException {
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(headerDoesNotExist(N8nNotificationService.SECRET_HEADER))
                .andExpect(content().json("""
                        {"type": "password_reset.requested",
                         "email": "user@example.com",
                         "name": "Existing User",
                         "resetLink": "http://localhost:3000/reset-password?token=xyz-789"}
                        """, true))
                .andRespond(withSuccess());

        new N8nNotificationService(restTemplate, WEBHOOK_URL, "", FRONTEND_URL)
                .sendPasswordResetRequested("user@example.com", "Existing User", "xyz-789");

        server.verify();
    }

    @Test
    void webhookFailureIsReportedAsIOException() {
        server.expect(requestTo(WEBHOOK_URL)).andRespond(withServerError());

        N8nNotificationService service = new N8nNotificationService(restTemplate, WEBHOOK_URL, "", FRONTEND_URL);

        assertThrows(IOException.class,
                () -> service.sendUserRegistered("new.user@example.com", "New User", "abc-123"));
    }
}
