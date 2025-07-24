package com.example.diaensho_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "app_usage_stats")
public class AppUsageStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private Long totalTimeInForeground;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and setters
    // ...
} 