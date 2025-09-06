package com.example.diaensho_backend.controllers;

import com.example.diaensho_backend.dto.AppUsageStatRequest;
import com.example.diaensho_backend.entities.AppUsageStat;
import com.example.diaensho_backend.entities.User;
import com.example.diaensho_backend.repositories.AppUsageStatRepository;
import com.example.diaensho_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage-stats")
public class UsageStatsController {
    @Autowired
    private AppUsageStatRepository appUsageStatRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public AppUsageStat createUsageStat(@RequestBody AppUsageStatRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        AppUsageStat stat = new AppUsageStat();
        stat.setPackageName(request.getPackageName());
        stat.setTotalTimeInForeground(request.getTotalTimeInForeground());
        stat.setDate(request.getDate());
        stat.setUser(user);
        
        return appUsageStatRepository.save(stat);
    }

    @PostMapping("/batch")
    public List<AppUsageStat> createUsageStats(@RequestBody List<AppUsageStatRequest> requests, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<AppUsageStat> stats = requests.stream().map(request -> {
            AppUsageStat stat = new AppUsageStat();
            stat.setPackageName(request.getPackageName());
            stat.setTotalTimeInForeground(request.getTotalTimeInForeground());
            stat.setDate(request.getDate());
            stat.setUser(user);
            return stat;
        }).toList();
        
        return appUsageStatRepository.saveAll(stats);
    }
}

