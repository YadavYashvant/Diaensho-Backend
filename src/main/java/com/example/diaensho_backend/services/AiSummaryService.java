package com.example.diaensho_backend.services;

import com.example.diaensho_backend.entities.*;
import com.example.diaensho_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    private DatabaseSearchService searchService;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key:YOUR_GEMINI_API_KEY}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
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
                long minutes = stat.getTotalTimeInForeground() / 60;
                prompt.append(stat.getPackageName()).append(": ")
                        .append(minutes).append(" minutes\n");
            }
            callGeminiAndSaveSummary(user, today, prompt.toString());
        }
    }

    private void callGeminiAndSaveSummary(User user, LocalDate date, String prompt) {
        WebClient webClient = webClientBuilder.build();

        GeminiRequest request = new GeminiRequest();
        request.contents = List.of(Map.of("parts", List.of(Map.of("text", prompt))));

        Mono<GeminiResponse> responseMono = webClient.post()
                .uri(GEMINI_API_URL + "?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class);

        responseMono.subscribe(
            response -> {
                String generatedText = extractGeneratedText(response);
                String content = extractContent(generatedText);
                String highlights = extractHighlights(generatedText);

                DailySummary summary = new DailySummary();
                summary.setUser(user);
                summary.setDate(date);
                summary.setContent(content);
                summary.setHighlights(highlights);
                dailySummaryRepository.save(summary);

                // Index in our database search service
                searchService.indexSummary(summary);
            },
            error -> {
                System.err.println("Error calling Gemini API: " + error.getMessage());
                // Create a simple fallback summary
                createFallbackSummary(user, date);
            }
        );
    }

    private String extractGeneratedText(GeminiResponse response) {
        try {
            return response.candidates.get(0).content.parts.get(0).text;
        } catch (Exception e) {
            return "Unable to generate summary due to API response format.";
        }
    }

    private String extractContent(String response) {
        if (response.contains("## Highlights")) {
            return response.split("## Highlights")[0].trim();
        }
        return response.trim();
    }

    private String extractHighlights(String response) {
        String[] parts = response.split("## Highlights");
        return parts.length > 1 ? parts[1].trim() : "No highlights identified.";
    }

    private void createFallbackSummary(User user, LocalDate date) {
        DailySummary summary = new DailySummary();
        summary.setUser(user);
        summary.setDate(date);
        summary.setContent("Summary generation temporarily unavailable. Please check your entries manually.");
        summary.setHighlights("Unable to generate highlights at this time.");
        dailySummaryRepository.save(summary);
    }

    static class GeminiRequest {
        public List<Map<String, Object>> contents;
    }

    static class GeminiResponse {
        public List<Candidate> candidates;

        static class Candidate {
            public Content content;

            static class Content {
                public List<Part> parts;

                static class Part {
                    public String text;
                }
            }
        }
    }
}
