package com.example.diaensho_backend.controllers;

import com.example.diaensho_backend.dto.DailySummaryResponse;
import com.example.diaensho_backend.entities.DailySummary;
import com.example.diaensho_backend.entities.User;
import com.example.diaensho_backend.repositories.DailySummaryRepository;
import com.example.diaensho_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summaries")
public class SummaryController {
    @Autowired
    private DailySummaryRepository dailySummaryRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public DailySummaryResponse getSummary(@RequestParam("date") String dateStr, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate date = LocalDate.parse(dateStr);
        DailySummary summary = dailySummaryRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new RuntimeException("Summary not found"));
        DailySummaryResponse response = new DailySummaryResponse();
        response.setContent(summary.getContent());
        response.setHighlights(summary.getHighlights());
        response.setDate(summary.getDate());
        return response;
    }
} 