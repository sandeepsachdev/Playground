package com.newscloud.model;

import java.time.Instant;
import java.util.List;

public record TrendingSnapshot(
        Instant generatedAt,
        int articleCount,
        List<String> sources,
        List<WordFrequency> words,
        List<ArticleSummary> articles,
        List<SourceWords> sourceWords
) {
}
