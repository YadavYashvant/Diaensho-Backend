package com.example.diaensho_backend.dto;

import java.time.LocalDate;

public class AppUsageStatRequest {
    private String packageName;
    private Long totalTimeInForeground;
    private LocalDate date;
    
    // Getters and setters
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getTotalTimeInForeground() {
        return totalTimeInForeground;
    }

    public void setTotalTimeInForeground(Long totalTimeInForeground) {
        this.totalTimeInForeground = totalTimeInForeground;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
