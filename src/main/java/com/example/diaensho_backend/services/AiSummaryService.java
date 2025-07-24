package com.example.diaensho_backend.services;

import com.example.diaensho_backend.entities.*;
import com.example.diaensho_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiSummaryService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryEntryRepository diaryEntryRepository;
    @Autowired
    private AppUsageStatRepository appUsageStatRepository;
    @Autowired
    private DailySummaryRepository dailySummaryRepository;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_GEMINI_API_KEY";
    private static final String PROMPT_TEMPLATE = "You are an insightful diary assistant. The following is a collection of raw, timestamped thoughts and app screen time data from a user's day. Your task is to: 1. Correct any spelling or grammatical errors in the thoughts. 2. Weave all the information into a cohesive, first-person narrative summary of the day. 3. Separately, identify 1-3 of the most inquisitive, creative, or exciting thoughts and list them under a '## Highlights' section.";

    @Scheduled(cron = "0 0 23 * * *")
    public void generateDailySummaries() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<DiaryEntry> entries = diaryEntryRepository.findAllByUserAndTimestampBetween(
                    user, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            List<AppUsageStat> stats = appUsageStatRepository.findAllByUserAndDate(user, today);
            if (entries.isEmpty() && stats.isEmpty()) continue;
            StringBuilder prompt = new StringBuilder(PROMPT_TEMPLATE + "\n\n");
            prompt.append("Diary Entries:\n");
            for (DiaryEntry entry : entries) {
                prompt.append("[").append(entry.getTimestamp()).append("] ").append(entry.getText()).append("\n");
            }
            prompt.append("\nApp Usage Stats:\n");
            for (AppUsageStat stat : stats) {
                prompt.append(stat.getPackageName()).append(": ")
                        .append(stat.getTotalTimeInForeground()).append(" seconds\n");
            }
            callGeminiAndSaveSummary(user, today, prompt.toString());
        }
    }

    private void callGeminiAndSaveSummary(User user, LocalDate date, String prompt) {
        WebClient webClient = webClientBuilder.build();
        Mono<String> responseMono = webClient.post()
                .uri(GEMINI_API_URL)
                .bodyValue(new GeminiRequest(prompt))
                .retrieve()
                .bodyToMono(String.class);
        responseMono.subscribe(response -> {
            String content = extractContent(response);
            String highlights = extractHighlights(response);
            DailySummary summary = new DailySummary();
            summary.setUser(user);
            summary.setDate(date);
            summary.setContent(content);
            summary.setHighlights(highlights);
            dailySummaryRepository.save(summary);
            elasticsearchService.indexSummary(summary);
        });
    }

    private String extractContent(String response) {
        // TODO: Parse the Gemini API response and extract the main narrative (before ## Highlights)
        return response.split("## Highlights")[0].trim();
    }

    private String extractHighlights(String response) {
        // TODO: Parse the Gemini API response and extract the highlights section
        String[] parts = response.split("## Highlights");
        return parts.length > 1 ? parts[1].trim() : "";
    }

    static class GeminiRequest {
        public String contents;
        public GeminiRequest(String prompt) { this.contents = prompt; }
    }
} 