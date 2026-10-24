package dev.ctrlspace.bootcamp_2025_03.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisteredEvent {
    private final String type = "user.registered";
    private String email;
    private String name;
    private String verificationLink;
}
