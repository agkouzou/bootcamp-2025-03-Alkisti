package dev.ctrlspace.bootcamp_2025_03.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetRequestedEvent {
    private final String type = "password_reset.requested";
    private String email;
    private String name;
    private String resetLink;
}
