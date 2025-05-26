package dev.ctrlspace.bootcamp_2025_03.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private Boolean isCompletion = true;
    private String completionModel;
    private Long threadId;
    private String threadName;
}
