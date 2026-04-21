package com.newscloud.service;

import com.newscloud.model.Article;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeedFetcherTest {

    private final FeedFetcher fetcher = new FeedFetcher();

    @Test
    void parsesRssEntries() throws Exception {
        List<Article> articles;
        try (InputStream stream = getClass().getResourceAsStream("/feeds/sample-rss.xml")) {
            assertThat(stream).isNotNull();
            articles = fetcher.parse(stream, "Test Wire");
        }

        assertThat(articles).hasSize(3);
        assertThat(articles).extracting(Article::title)
                .containsExactly(
                        "Climate summit opens in Sydney",
                        "Election counting continues",
                        "Sydney transport upgrade announced");
        assertThat(articles).allSatisfy(a -> assertThat(a.source()).isEqualTo("Test Wire"));
        assertThat(articles.get(0).description()).contains("Sydney");
    }
}
