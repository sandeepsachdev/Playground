package com.newscloud.controller;

import com.newscloud.service.TrendingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CloudViewController {

    private final TrendingService trendingService;

    public CloudViewController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping("/")
    public String cloud(Model model) {
        model.addAttribute("snapshot", trendingService.snapshot());
        return "cloud";
    }
}
