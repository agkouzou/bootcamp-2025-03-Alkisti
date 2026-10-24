package dev.ctrlspace.bootcamp_2025_03.services;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SendGridEmailServiceTest {

    private final SendGrid sendGrid = mock(SendGrid.class);
    private SendGridEmailService service;

    @BeforeEach
    void setUp() {
        service = spy(new SendGridEmailService());
        ReflectionTestUtils.setField(service, "sendgridApiKey", "SG.test");
        ReflectionTestUtils.setField(service, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:3000");
        doReturn(sendGrid).when(service).createClient();
    }

    @Test
    void acceptedEmailSucceeds() throws IOException {
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(202, "", Map.of()));

        assertDoesNotThrow(() -> service.sendVerificationEmail("user@example.com", "abc-123"));
    }

    @Test
    void rejectedEmailThrowsWithSendGridStatusAndBody() throws IOException {
        when(sendGrid.api(any(Request.class)))
                .thenReturn(new Response(401, "{\"errors\":[{\"message\":\"Maximum credits exceeded\"}]}", Map.of()));

        IOException ex = assertThrows(IOException.class,
                () -> service.sendPasswordResetEmail("user@example.com", "xyz-789"));

        assertTrue(ex.getMessage().contains("password reset email"));
        assertTrue(ex.getCause().getMessage().contains("401"));
        assertTrue(ex.getCause().getMessage().contains("Maximum credits exceeded"));
    }

    @Test
    void networkErrorThrowsWithOriginalCause() throws IOException {
        IOException networkError = new IOException("connection refused");
        when(sendGrid.api(any(Request.class))).thenThrow(networkError);

        IOException ex = assertThrows(IOException.class,
                () -> service.sendVerificationEmail("user@example.com", "abc-123"));

        assertSame(networkError, ex.getCause());
    }
}
