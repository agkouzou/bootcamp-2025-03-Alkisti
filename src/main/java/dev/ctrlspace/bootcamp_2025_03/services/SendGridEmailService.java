package dev.ctrlspace.bootcamp_2025_03.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SendGridEmailService {

    @Value("${SENDGRID_API_KEY:}")
    private String sendgridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendVerificationEmail(String toEmail, String token) throws IOException {
        if (sendgridApiKey.isBlank() || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY or SENDGRID_FROM_EMAIL is not set");
        }

        Email from = new Email(fromEmail);
        String subject = "Verify your email address";
        Email to = new Email(toEmail);

        String verificationLink = frontendBaseUrl + "/login?token=" + token;

        Content content = new Content("text/plain",
                "Welcome! Please verify your account by clicking this link: " + verificationLink);

        send(new Mail(from, subject, to, content), "verification email");
    }

    public void sendPasswordResetEmail(String toEmail, String token) throws IOException {
        if (sendgridApiKey.isBlank() || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY or SENDGRID_FROM_EMAIL is not set");
        }

        Email from = new Email(fromEmail);
        String subject = "Password Reset Request";
        Email to = new Email(toEmail);

        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;

        Content content = new Content("text/plain",
                "You requested to reset your password. Click this link to set a new password: " + resetLink
                        + "\nIf you didn't request this, please ignore this email.");

        send(new Mail(from, subject, to, content), "password reset email");
    }

    private void send(Mail mail, String description) throws IOException {
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = createClient().api(request);
            log.info("SendGrid {} status: {}", description, response.getStatusCode());

            if (response.getStatusCode() >= 400) {
                throw new IOException("SendGrid rejected the " + description + ": "
                        + response.getStatusCode() + " " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Failed to send " + description, ex);
        }
    }

    SendGrid createClient() {
        return new SendGrid(sendgridApiKey);
    }
}
