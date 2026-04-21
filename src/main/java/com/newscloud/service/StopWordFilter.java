package com.newscloud.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads a stop-word list from the classpath ({@code stopwords.txt}) and
 * exposes a fast membership check. Lines starting with {@code #} and blank
 * lines are ignored; all words are compared case-insensitively.
 */
@Component
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        this("stopwords.txt");
    }

    StopWordFilter(String resource) {
        this.stopWords = loadStopWords(resource);
    }

    StopWordFilter(Set<String> stopWords) {
        this.stopWords = Set.copyOf(stopWords);
    }

    public boolean isStopWord(String word) {
        return word != null && stopWords.contains(word.toLowerCase());
    }

    public int size() {
        return stopWords.size();
    }

    private static Set<String> loadStopWords(String resource) {
        Set<String> words = new HashSet<>();
        ClassPathResource cp = new ClassPathResource(resource);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(cp.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                words.add(trimmed.toLowerCase());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load stop-word list " + resource, e);
        }
        return Collections.unmodifiableSet(words);
    }
}
