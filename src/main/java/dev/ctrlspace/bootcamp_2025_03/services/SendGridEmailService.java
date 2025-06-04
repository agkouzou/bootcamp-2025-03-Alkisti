package dev.ctrlspace.bootcamp_2025_03.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${SENDGRID_API_KEY:}")
    private String sendgridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) throws IOException {
        if (sendgridApiKey.isBlank() || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY or SENDGRID_FROM_EMAIL is not set");
        }

        Email from = new Email(fromEmail);
        String subject = "Verify your email address";
        Email to = new Email(toEmail);

        String verificationLink = "http://localhost:8080/users/verify?token=" + token;
        Content content = new Content("text/plain",
                "Welcome! Please verify your account by clicking this link: " + verificationLink);

        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Email send status: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new IOException("Failed to send verification email", ex);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) throws IOException {
        if (sendgridApiKey.isBlank() || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY or SENDGRID_FROM_EMAIL is not set");
        }

        Email from = new Email(fromEmail);
        String subject = "Password Reset Request";
        Email to = new Email(toEmail);

        String resetLink = "http://localhost:8080/users/password-reset?token=" + token;

        Content content = new Content("text/plain",
                "You requested to reset your password. Click this link to set a new password: " + resetLink
                        + "\nIf you didn't request this, please ignore this email.");

        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendgridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Password reset email send status: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new IOException("Failed to send password reset email", ex);
        }
    }
}
