package com.newscloud.service;

import com.newscloud.config.FeedProperties.FeedSource;
import com.newscloud.model.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches an RSS or Atom feed via HTTP and maps entries onto {@link Article}.
 * Uses the JDK {@link HttpClient} so proxies/redirects/compression are handled
 * uniformly, then hands the stream to Rome for parsing.
 */
@Component
public class FeedFetcher {

    private static final Logger log = LoggerFactory.getLogger(FeedFetcher.class);

    private final HttpClient httpClient;

    public FeedFetcher() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    FeedFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Article> fetch(FeedSource source) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(source.getUrl()))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "news-wordcloud/1.0 (+https://github.com/)")
                .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() / 100 != 2) {
                response.body().close();
                throw new IOException("HTTP " + response.statusCode() + " from " + source.getUrl());
            }
            try (InputStream body = response.body()) {
                return parse(body, source.getName());
            }
        } catch (IOException | InterruptedException | FeedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Feed fetch failed for {} ({}): {}", source.getName(), source.getUrl(), ex.getMessage());
            return List.of();
        }
    }

    List<Article> parse(InputStream stream, String sourceName) throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        input.setAllowDoctypes(false);
        SyndFeed feed = input.build(new XmlReader(stream));
        List<Article> articles = new ArrayList<>(feed.getEntries().size());
        for (SyndEntry entry : feed.getEntries()) {
            articles.add(toArticle(entry, sourceName));
        }
        return articles;
    }

    private Article toArticle(SyndEntry entry, String sourceName) {
        String description = entry.getDescription() != null
                ? entry.getDescription().getValue()
                : "";
        Instant published = entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant()
                : (entry.getUpdatedDate() != null ? entry.getUpdatedDate().toInstant() : Instant.now());
        return new Article(
                safe(entry.getTitle()),
                safe(description),
                sourceName,
                entry.getLink(),
                published
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
