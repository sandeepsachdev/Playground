package com.newscloud.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Loads a stop-word list from the classpath ({@code stopwords.txt}) and
 * exposes a fast membership check. Lines starting with {@code #} and blank
 * lines are ignored; all words are compared case-insensitively. Lines
 * prefixed with {@code ~} are treated as substring fragments — any token
 * containing the fragment is filtered.
 */
@Component
public class StopWordFilter {

    private final Set<String> stopWords;
    private final List<String> stopFragments;

    public StopWordFilter() {
        this("stopwords.txt");
    }

    StopWordFilter(String resource) {
        Loaded loaded = loadStopWords(resource);
        this.stopWords = loaded.words;
        this.stopFragments = loaded.fragments;
    }

    StopWordFilter(Set<String> stopWords) {
        this(stopWords, List.of());
    }

    StopWordFilter(Set<String> stopWords, List<String> stopFragments) {
        this.stopWords = Set.copyOf(stopWords);
        this.stopFragments = List.copyOf(stopFragments);
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

    private static Loaded loadStopWords(String resource) {
        Set<String> words = new HashSet<>();
        List<String> fragments = new ArrayList<>();
        ClassPathResource cp = new ClassPathResource(resource);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(cp.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String lower = trimmed.toLowerCase(Locale.ROOT);
                if (lower.startsWith("~")) {
                    String fragment = lower.substring(1).trim();
                    if (!fragment.isEmpty()) {
                        fragments.add(fragment);
                    }
                } else {
                    words.add(lower);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load stop-word list " + resource, e);
        }
        return new Loaded(Collections.unmodifiableSet(words),
                Collections.unmodifiableList(fragments));
    }

    private record Loaded(Set<String> words, List<String> fragments) {
    }
}
