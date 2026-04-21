package com.newscloud.service;

import com.newscloud.model.StopWord;
import com.newscloud.repository.StopWordRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Database-backed stop-word filter. On startup (and after each mutation) the
 * filter snapshots the {@code stop_words} table into two in-memory structures
 * for fast membership checks:
 * <ul>
 *   <li>exact matches — token equals entry</li>
 *   <li>fragment matches — token contains entry (for rows flagged as fragments)</li>
 * </ul>
 * All comparisons are case-insensitive.
 */
@Component
public class StopWordFilter {

    private static final Logger log = LoggerFactory.getLogger(StopWordFilter.class);

    private final StopWordRepository repository;

    private volatile Set<String> stopWords = Set.of();
    private volatile List<String> stopFragments = List.of();

    @Autowired
    public StopWordFilter(StopWordRepository repository) {
        this.repository = repository;
    }

    /** Test hook: build a filter without a database, from literal collections. */
    StopWordFilter(Set<String> stopWords, List<String> stopFragments) {
        this.repository = null;
        this.stopWords = Set.copyOf(stopWords);
        this.stopFragments = List.copyOf(stopFragments);
    }

    @PostConstruct
    public void reload() {
        if (repository == null) {
            return;
        }
        Set<String> words = new HashSet<>();
        List<String> fragments = new ArrayList<>();
        for (StopWord row : repository.findAll()) {
            String lower = row.getWord().toLowerCase(Locale.ROOT);
            if (row.isFragment()) {
                fragments.add(lower);
            } else {
                words.add(lower);
            }
        }
        this.stopWords = Collections.unmodifiableSet(words);
        this.stopFragments = Collections.unmodifiableList(fragments);
        log.info("Loaded {} stop-word entries ({} exact, {} fragments)",
                words.size() + fragments.size(), words.size(), fragments.size());
    }

    public boolean isStopWord(String word) {
        if (word == null) {
            return false;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (stopWords.contains(lower)) {
            return true;
        }
        for (String fragment : stopFragments) {
            if (lower.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return stopWords.size() + stopFragments.size();
    }

    public Set<String> exactWords() {
        return stopWords;
    }

    public List<String> fragments() {
        return stopFragments;
    }

    /**
     * Adds a stop word to the database (if not already present) and refreshes
     * the in-memory snapshot. Returns {@code true} if a new row was inserted.
     */
    public synchronized boolean addWord(String word, boolean fragment) {
        if (repository == null) {
            throw new IllegalStateException("StopWordFilter constructed without a repository");
        }
        if (word == null) {
            return false;
        }
        String lower = word.trim().toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) {
            return false;
        }
        Optional<StopWord> existing = repository.findByWord(lower);
        if (existing.isPresent()) {
            return false;
        }
        repository.save(new StopWord(lower, fragment));
        reload();
        return true;
    }
}
