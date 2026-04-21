package com.newscloud.controller;

import com.newscloud.model.TrendingSnapshot;
import com.newscloud.service.TrendingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trending")
public class TrendingRestController {

    private final TrendingService trendingService;

    public TrendingRestController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping
    public TrendingSnapshot trending() {
        return trendingService.snapshot();
    }

    @PostMapping("/refresh")
    public TrendingSnapshot refresh() {
        return trendingService.refresh();
    }
}
