package com.newscloud.service;

import com.newscloud.config.FeedProperties;
import com.newscloud.config.FeedProperties.FeedSource;
import com.newscloud.model.Article;
import com.newscloud.model.TrendingSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrendingServiceTest {

    private static StopWordFilter testStopWordFilter() {
        return new StopWordFilter(
                Set.of("the", "and", "in", "for", "a"),
                List.of());
    }

    @Test
    void aggregatesFromConfiguredFeedsAndCachesSnapshot() {
        FeedProperties properties = new FeedProperties();
        properties.setFeeds(List.of(new FeedSource("Fake Wire", "https://example.com/feed.xml")));
        properties.setTopN(20);

        List<Article> fixture = List.of(
                new Article("Sydney summit climate", "Climate summit in Sydney", "Fake Wire",
                        "https://example.com/1", Instant.now()),
                new Article("Climate talks continue", "Climate talks continue in Sydney",
                        "Fake Wire", "https://example.com/2", Instant.now()),
                new Article("Election results", "Election results delayed", "Fake Wire",
                        "https://example.com/3", Instant.now())
        );
        FeedFetcher fetcher = new FeedFetcher() {
            @Override
            public List<Article> fetch(FeedSource source) {
                return fixture;
            }
        };
        TextAnalyzer analyzer = new TextAnalyzer(testStopWordFilter(), properties);
        TrendingService service = new TrendingService(properties, fetcher, analyzer);

        TrendingSnapshot snapshot = service.snapshot();

        assertThat(snapshot.articleCount()).isEqualTo(3);
        assertThat(snapshot.sources()).containsExactly("Fake Wire");
        // "climate" is suppressed because it appears inside the "climate talks"
        // phrase that survives in the cloud — surface the phrase in its place.
        assertThat(snapshot.words()).extracting(wf -> wf.text()).contains("sydney", "climate talks");

        // Cached: second call returns the same instance without refetching.
        TrendingSnapshot again = service.snapshot();
        assertThat(again).isSameAs(snapshot);
    }

    @Test
    void deduplicatesArticlesByLink() {
        FeedProperties properties = new FeedProperties();
        properties.setFeeds(List.of(
                new FeedSource("Wire A", "https://a"),
                new FeedSource("Wire B", "https://b")));
        Article shared = new Article("Shared story", "Shared story body", "Wire A",
                "https://example.com/same", Instant.now());
        FeedFetcher fetcher = new FeedFetcher() {
            @Override
            public List<Article> fetch(FeedSource source) {
                return List.of(shared);
            }
        };
        TrendingService service = new TrendingService(properties, fetcher,
                new TextAnalyzer(testStopWordFilter(), properties));

        TrendingSnapshot snapshot = service.snapshot();
        assertThat(snapshot.articleCount()).isEqualTo(1);
        assertThat(snapshot.sources()).containsExactly("Wire A", "Wire B");
    }

    @Test
    void returnsEmptySnapshotWhenNoFeedsConfigured() {
        FeedProperties properties = new FeedProperties();
        TrendingService service = new TrendingService(properties, new FeedFetcher(),
                new TextAnalyzer(testStopWordFilter(), properties));

        TrendingSnapshot snapshot = service.snapshot();
        assertThat(snapshot.articleCount()).isZero();
        assertThat(snapshot.words()).isEmpty();
        assertThat(snapshot.sources()).isEmpty();
    }

    @Test
    void refreshRebuildsWhenCacheExpired() {
        FeedProperties properties = new FeedProperties();
        properties.setFeeds(List.of(new FeedSource("Wire", "https://a")));
        int[] calls = {0};
        FeedFetcher fetcher = new FeedFetcher() {
            @Override
            public List<Article> fetch(FeedSource source) {
                calls[0]++;
                return List.of(new Article("t" + calls[0], "body" + calls[0], "Wire",
                        "https://example.com/" + calls[0], Instant.now()));
            }
        };
        TrendingService service = new TrendingService(properties, fetcher,
                new TextAnalyzer(testStopWordFilter(), properties));

        service.snapshot();
        // Force cache expiry by rewinding the cached snapshot's timestamp.
        TrendingSnapshot current = service.snapshot();
        ReflectionTestUtils.setField(service, "cached",
                new TrendingSnapshot(Instant.now().minusSeconds(60 * 60 * 24),
                        current.articleCount(), current.sources(), current.words(),
                        current.articles(), current.sourceWords()));

        service.snapshot();
        assertThat(calls[0]).isEqualTo(2);
    }
}
