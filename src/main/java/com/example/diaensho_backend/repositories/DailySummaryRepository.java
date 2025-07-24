package com.example.diaensho_backend.repositories;

import com.example.diaensho_backend.entities.DailySummary;
import com.example.diaensho_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    Optional<DailySummary> findByUserAndDate(User user, LocalDate date);
    List<DailySummary> findAllByUser(User user);
} 