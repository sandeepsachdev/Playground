package com.newscloud.controller;

import com.newscloud.service.StopWordFilter;
import com.newscloud.service.TrendingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

    @GetMapping
    public Map<String, Object> list() {
        return Map.of(
                "size", stopWordFilter.size(),
                "exact", new TreeSet<>(stopWordFilter.exactWords()),
                "fragments", List.copyOf(stopWordFilter.fragments()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddRequest request) {
        if (request == null || request.word() == null || request.word().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "word is required"));
        }
        boolean fragment = Boolean.TRUE.equals(request.fragment());
        boolean added = stopWordFilter.addWord(request.word(), fragment);
        // Always invalidate so the next /api/trending rebuilds with the current
        // filter — the user clicked Exclude because they want the term gone,
        // whether or not the row was already in the database.
        trendingService.invalidate();
        return ResponseEntity.ok(Map.of(
                "word", request.word().trim().toLowerCase(),
                "fragment", fragment,
                "added", added,
                "size", stopWordFilter.size()));
    }
}
