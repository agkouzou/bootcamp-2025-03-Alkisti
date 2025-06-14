package dev.ctrlspace.bootcamp_2025_03.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadResponse {
    private Long id;
    private String title;
    private String completionModel;
    private List<MessageResponse> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
