package com.newscloud.model;

public record ArticleSummary(
        String title,
        String description,
        String link,
        String source
) {
}
