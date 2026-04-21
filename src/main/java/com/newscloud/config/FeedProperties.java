package com.newscloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "newscloud")
public class FeedProperties {

    /** RSS/Atom feed URLs to aggregate. */
    private List<FeedSource> feeds = new ArrayList<>();

    /** How long to cache a successful aggregation before refetching. */
    private Duration refreshInterval = Duration.ofMinutes(15);

    /** Maximum number of unique entries to carry into the word-cloud analysis. */
    private int maxEntries = 500;

    /** Minimum token length to include in the cloud (filters stray letters). */
    private int minTokenLength = 3;

    /** Maximum words + phrases to return from the trending endpoint. */
    private int topN = 120;

    /** Include two-word phrases (bigrams) alongside single words. */
    private boolean includePhrases = true;

    public List<FeedSource> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<FeedSource> feeds) {
        this.feeds = feeds;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public int getMinTokenLength() {
        return minTokenLength;
    }

    public void setMinTokenLength(int minTokenLength) {
        this.minTokenLength = minTokenLength;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public boolean isIncludePhrases() {
        return includePhrases;
    }

    public void setIncludePhrases(boolean includePhrases) {
        this.includePhrases = includePhrases;
    }

    public static class FeedSource {
        private String name;
        private String url;

        public FeedSource() {
        }

        public FeedSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
