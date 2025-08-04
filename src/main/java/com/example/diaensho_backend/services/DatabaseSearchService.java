package com.example.diaensho_backend.services;

import com.example.diaensho_backend.dto.SearchResponse;
import com.example.diaensho_backend.entities.DailySummary;
import com.example.diaensho_backend.entities.User;
import com.example.diaensho_backend.repositories.DailySummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseSearchService {
    @Autowired
    private DailySummaryRepository dailySummaryRepository;

    public void indexSummary(DailySummary summary) {
        // No need to index separately since we're using database search
        // This method is kept for compatibility with existing code
    }

    public List<SearchResponse> searchSummaries(String query, User user) {
        List<DailySummary> summaries = dailySummaryRepository.findAllByUser(user);
        
        // Simple text search in content and highlights
        return summaries.stream()
                .filter(summary -> containsIgnoreCase(summary.getContent(), query) || 
                                 containsIgnoreCase(summary.getHighlights(), query))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String text, String query) {
        if (text == null || query == null) return false;
        return text.toLowerCase().contains(query.toLowerCase());
    }

    private SearchResponse toResponse(DailySummary summary) {
        SearchResponse response = new SearchResponse();
        response.setId(summary.getId());
        response.setContent(summary.getContent());
        response.setHighlights(summary.getHighlights());
        response.setDate(summary.getDate());
        return response;
    }
}
