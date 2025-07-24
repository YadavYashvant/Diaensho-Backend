package com.example.diaensho_backend.repositories;

import com.example.diaensho_backend.entities.DiaryEntry;
import com.example.diaensho_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {
    List<DiaryEntry> findAllByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
} 