package com.newscloud.controller;

import com.newscloud.config.FeedProperties;
import com.newscloud.model.TrendingSnapshot;
import com.newscloud.service.TrendingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CloudViewController {

    private final TrendingService trendingService;
    private final FeedProperties feedProperties;

    public CloudViewController(TrendingService trendingService, FeedProperties feedProperties) {
        this.trendingService = trendingService;
        this.feedProperties = feedProperties;
    }

    @GetMapping("/")
    public String cloud(Model model) {
        TrendingSnapshot snapshot = trendingService.snapshot();
        model.addAttribute("snapshot", snapshot);
        model.addAttribute("nextRefreshAt", snapshot.generatedAt().plus(feedProperties.getRefreshInterval()));
        return "cloud";
    }
}
