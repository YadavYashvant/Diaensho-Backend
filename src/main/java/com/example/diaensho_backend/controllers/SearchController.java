package com.example.diaensho_backend.controllers;

import com.example.diaensho_backend.dto.SearchResponse;
import com.example.diaensho_backend.entities.User;
import com.example.diaensho_backend.repositories.UserRepository;
import com.example.diaensho_backend.services.DatabaseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Autowired
    private DatabaseSearchService searchService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<SearchResponse> search(@RequestParam("q") String query, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return searchService.searchSummaries(query, user);
    }
}

