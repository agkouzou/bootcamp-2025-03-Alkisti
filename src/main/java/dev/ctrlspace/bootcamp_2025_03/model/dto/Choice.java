package dev.ctrlspace.bootcamp_2025_03.model.dto;

// Choice.java
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choice {
    private int index;
    private ChatMessage message;
    private Object logprobs;            // null or a more detailed type if you know it
    @JsonProperty("finish_reason")
    private String finishReason;

    // getters & setters omitted for brevity
}

