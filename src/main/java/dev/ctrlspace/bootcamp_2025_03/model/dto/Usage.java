package dev.ctrlspace.bootcamp_2025_03.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Usage.java
public class Usage {
    @JsonProperty("queue_time")
    private double queueTime;
    @JsonProperty("prompt_tokens")
    private int promptTokens;
    @JsonProperty("prompt_time")
    private double promptTime;
    @JsonProperty("completion_tokens")
    private int completionTokens;
    @JsonProperty("completion_time")
    private double completionTime;
    @JsonProperty("total_tokens")
    private int totalTokens;
    @JsonProperty("total_time")
    private double totalTime;

    // getters & setters omitted for brevity
}
