package com.example.diaensho_backend.services;

import com.example.diaensho_backend.dto.SearchResponse;
import com.example.diaensho_backend.entities.DailySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticsearchService {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void indexSummary(DailySummary summary) {
        elasticsearchRestTemplate.save(summary);
    }

    public List<SearchResponse> searchSummaries(String query, Long userId) {
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery()
                        .must(multiMatchQuery(query, "content", "highlights"))
                        .filter(termQuery("user.id", userId)))
                .build();
        SearchHits<DailySummary> hits = elasticsearchRestTemplate.search(searchQuery, DailySummary.class);
        return hits.getSearchHits().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private SearchResponse toResponse(SearchHit<DailySummary> hit) {
        DailySummary summary = hit.getContent();
        SearchResponse response = new SearchResponse();
        response.setId(summary.getId());
        response.setContent(summary.getContent());
        response.setHighlights(summary.getHighlights());
        response.setDate(summary.getDate());
        return response;
    }
} 