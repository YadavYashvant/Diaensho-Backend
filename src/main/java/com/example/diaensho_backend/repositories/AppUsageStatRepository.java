package com.example.diaensho_backend.repositories;

import com.example.diaensho_backend.entities.AppUsageStat;
import com.example.diaensho_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppUsageStatRepository extends JpaRepository<AppUsageStat, Long> {
    List<AppUsageStat> findAllByUserAndDate(User user, LocalDate date);
} 