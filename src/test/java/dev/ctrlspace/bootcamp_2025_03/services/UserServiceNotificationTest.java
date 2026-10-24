package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceNotificationTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final SendGridEmailService sendGrid = mock(SendGridEmailService.class);
    private final N8nNotificationService n8n = mock(N8nNotificationService.class);

    private final UserService userService = new UserService(userRepository, passwordEncoder, sendGrid, n8n);

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static User newSignup() {
        User user = new User();
        user.setName("New User");
        user.setEmail("new.user@example.com");
        user.setPassword("123456");
        return user;
    }

    @Test
    void signupUsesN8nWhenEnabled() throws Exception {
        when(n8n.isEnabled()).thenReturn(true);

        User created = userService.create(newSignup());

        verify(n8n).sendUserRegistered("new.user@example.com", "New User", created.getVerificationToken());
        verify(sendGrid, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void signupUsesSendGridWhenN8nDisabled() throws Exception {
        when(n8n.isEnabled()).thenReturn(false);

        User created = userService.create(newSignup());

        verify(sendGrid).sendVerificationEmail("new.user@example.com", created.getVerificationToken());
        verify(n8n, never()).sendUserRegistered(anyString(), anyString(), anyString());
    }

    @Test
    void signupFailsWith500WhenN8nCallFails() throws Exception {
        when(n8n.isEnabled()).thenReturn(true);
        IOException n8nFailure = new IOException("n8n down");
        doThrow(n8nFailure).when(n8n).sendUserRegistered(anyString(), anyString(), anyString());

        BootcampException ex = assertThrows(BootcampException.class, () -> userService.create(newSignup()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertSame(n8nFailure, ex.getCause());
    }

    @Test
    void passwordResetUsesN8nWhenEnabled() throws Exception {
        User existing = newSignup();
        when(userRepository.findByEmail("new.user@example.com")).thenReturn(Optional.of(existing));
        when(n8n.isEnabled()).thenReturn(true);

        userService.initiatePasswordReset("new.user@example.com");

        verify(n8n).sendPasswordResetRequested(eq("new.user@example.com"), eq("New User"), eq(existing.getPasswordResetToken()));
        verify(sendGrid, never()).sendPasswordResetEmail(anyString(), anyString());
    }
}
