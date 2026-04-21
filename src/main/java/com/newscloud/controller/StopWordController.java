package com.newscloud.controller;

import com.newscloud.service.StopWordFilter;
import com.newscloud.service.TrendingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stopwords")
public class StopWordController {

    private final StopWordFilter stopWordFilter;
    private final TrendingService trendingService;

    public StopWordController(StopWordFilter stopWordFilter, TrendingService trendingService) {
        this.stopWordFilter = stopWordFilter;
        this.trendingService = trendingService;
    }

    public record AddRequest(String word, Boolean fragment) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddRequest request) {
        if (request == null || request.word() == null || request.word().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "word is required"));
        }
        boolean fragment = Boolean.TRUE.equals(request.fragment());
        boolean added = stopWordFilter.addWord(request.word(), fragment);
        if (added) {
            trendingService.invalidate();
        }
        return ResponseEntity.ok(Map.of(
                "word", request.word().trim().toLowerCase(),
                "fragment", fragment,
                "added", added,
                "size", stopWordFilter.size()));
    }
}
