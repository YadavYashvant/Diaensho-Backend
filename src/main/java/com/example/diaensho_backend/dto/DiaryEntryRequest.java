package com.example.diaensho_backend.dto;

import java.time.LocalDateTime;

public class DiaryEntryRequest {
    private String text;
    private LocalDateTime timestamp;

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
