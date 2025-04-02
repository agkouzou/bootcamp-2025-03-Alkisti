package dev.ctrlspace.bootcamp_2025_03.web_simpe_java_examples.day_3.model;

import java.time.Instant;

public class Message {

    private String textValue;
    private String sender;
    private Instant createdAt;

    public Message() {
    }

    public Message(String textValue, String sender, Instant createdAt) {
        this.textValue = textValue;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Message{" +
                "textValue='" + textValue + '\'' +
                ", sender='" + sender + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
