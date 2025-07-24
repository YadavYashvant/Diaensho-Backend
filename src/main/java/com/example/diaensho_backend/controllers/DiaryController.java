package com.example.diaensho_backend.controllers;

import com.example.diaensho_backend.dto.DiaryEntryRequest;
import com.example.diaensho_backend.entities.DiaryEntry;
import com.example.diaensho_backend.entities.User;
import com.example.diaensho_backend.repositories.DiaryEntryRepository;
import com.example.diaensho_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entries")
public class DiaryController {
    @Autowired
    private DiaryEntryRepository diaryEntryRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public DiaryEntry createEntry(@RequestBody DiaryEntryRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        DiaryEntry entry = new DiaryEntry();
        entry.setText(request.getText());
        entry.setTimestamp(request.getTimestamp());
        entry.setUser(user);
        return diaryEntryRepository.save(entry);
    }
} 