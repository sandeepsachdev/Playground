package com.newscloud.service;

import com.newscloud.config.FeedProperties;
import com.newscloud.config.FeedProperties.FeedSource;
import com.newscloud.model.Article;
import com.newscloud.model.ArticleSummary;
import com.newscloud.model.SourceWords;
import com.newscloud.model.TrendingSnapshot;
import com.newscloud.model.WordFrequency;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aggregates articles from every configured feed, de-duplicates by link, ranks
 * them newest-first, hands the top {@code maxEntries} to the text analyzer, and
 * caches the resulting snapshot for {@code refreshInterval}.
 */
@Service
public class TrendingService {

    private static final Logger log = LoggerFactory.getLogger(TrendingService.class);

    private final FeedProperties properties;
    private final FeedFetcher feedFetcher;
    private final TextAnalyzer textAnalyzer;

    private volatile TrendingSnapshot cached;

    public TrendingService(FeedProperties properties,
                           FeedFetcher feedFetcher,
                           TextAnalyzer textAnalyzer) {
        this.properties = properties;
        this.feedFetcher = feedFetcher;
        this.textAnalyzer = textAnalyzer;
    }

    public TrendingSnapshot snapshot() {
        TrendingSnapshot snapshot = cached;
        if (snapshot != null && !isExpired(snapshot)) {
            return snapshot;
        }
        return refresh();
    }

    public synchronized void invalidate() {
        cached = null;
    }

    public synchronized TrendingSnapshot rebuild() {
        cached = null;
        return refresh();
    }

    public synchronized TrendingSnapshot refresh() {
        if (cached != null && !isExpired(cached)) {
            return cached;
        }
        if (properties.getFeeds().isEmpty()) {
            log.warn("No feeds configured — returning empty snapshot");
            cached = empty();
            return cached;
        }

        List<Article> articles = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        Set<String> seenLinks = new HashSet<>();
        for (FeedSource feed : properties.getFeeds()) {
            List<Article> fetched = feedFetcher.fetch(feed);
            if (fetched.isEmpty()) {
                continue;
            }
            sources.add(feed.getName());
            for (Article article : fetched) {
                String key = article.link() != null ? article.link() : article.title();
                if (seenLinks.add(key)) {
                    articles.add(article);
                }
            }
        }

        articles.sort(Comparator.comparing(Article::publishedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (articles.size() > properties.getMaxEntries()) {
            articles = articles.subList(0, properties.getMaxEntries());
        }

        List<WordFrequency> words = textAnalyzer.analyze(articles);
        List<ArticleSummary> summaries = articles.stream().map(TrendingService::toSummary).toList();
        List<SourceWords> perSource = analyzePerSource(articles, sources);
        cached = new TrendingSnapshot(Instant.now(), articles.size(), sources, words, summaries, perSource);
        log.info("Refreshed trending snapshot: {} articles across {} sources, {} ranked terms",
                articles.size(), sources.size(), words.size());
        return cached;
    }

    private List<SourceWords> analyzePerSource(List<Article> articles, List<String> orderedSources) {
        Map<String, List<Article>> bySource = new LinkedHashMap<>();
        for (String name : orderedSources) {
            bySource.put(name, new ArrayList<>());
        }
        for (Article article : articles) {
            bySource.computeIfAbsent(article.source(), k -> new ArrayList<>()).add(article);
        }
        List<SourceWords> result = new ArrayList<>(bySource.size());
        for (Map.Entry<String, List<Article>> entry : bySource.entrySet()) {
            List<Article> sourceArticles = entry.getValue();
            if (sourceArticles.isEmpty()) {
                continue;
            }
            result.add(new SourceWords(entry.getKey(), textAnalyzer.analyze(sourceArticles)));
        }
        return result;
    }

    private TrendingSnapshot empty() {
        return new TrendingSnapshot(Instant.now(), 0, List.of(), List.of(), List.of(), List.of());
    }

    private static ArticleSummary toSummary(Article article) {
        return new ArticleSummary(
                plainText(article.title()),
                truncate(plainText(article.description()), 240),
                article.link(),
                article.source());
    }

    private static String plainText(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return Jsoup.parse(raw).text().trim();
    }

    private static String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        int cut = text.lastIndexOf(' ', max);
        if (cut < max / 2) {
            cut = max;
        }
        return text.substring(0, cut).trim() + "…";
    }

    private boolean isExpired(TrendingSnapshot snapshot) {
        return Duration.between(snapshot.generatedAt(), Instant.now())
                .compareTo(properties.getRefreshInterval()) > 0;
    }
}
