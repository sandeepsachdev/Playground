package com.newscloud.service;

import com.newscloud.config.FeedProperties;
import com.newscloud.model.Article;
import com.newscloud.model.WordFrequency;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Tokenizes article text and ranks the most frequent words and two-word
 * phrases after stop-word filtering.
 */
@Component
public class TextAnalyzer {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}0-9']+");
    private static final Pattern LEADING_TRAILING_APOSTROPHES = Pattern.compile("^'+|'+$");

    private final StopWordFilter stopWordFilter;
    private final FeedProperties properties;

    public TextAnalyzer(StopWordFilter stopWordFilter, FeedProperties properties) {
        this.stopWordFilter = stopWordFilter;
        this.properties = properties;
    }

    public List<WordFrequency> analyze(List<Article> articles) {
        Map<String, Integer> wordCounts = new HashMap<>();
        Map<String, Integer> phraseCounts = new HashMap<>();

        for (Article article : articles) {
            List<String> tokens = tokenize(article.title() + " " + article.description());
            List<String> kept = new ArrayList<>(tokens.size());
            for (String token : tokens) {
                if (shouldKeep(token)) {
                    kept.add(token);
                    wordCounts.merge(token, 1, Integer::sum);
                }
            }
            if (properties.isIncludePhrases()) {
                countBigrams(kept, phraseCounts);
            }
        }

        List<WordFrequency> merged = new ArrayList<>(wordCounts.size() + phraseCounts.size());
        wordCounts.forEach((word, count) -> merged.add(new WordFrequency(word, count, false)));
        phraseCounts.forEach((phrase, count) -> {
            if (count >= 2 && !stopWordFilter.isStopWord(phrase)) {
                merged.add(new WordFrequency(phrase, count, true));
            }
        });

        merged.sort(Comparator.comparingInt(WordFrequency::count).reversed()
                .thenComparing(WordFrequency::text));
        return takeTopWithPhraseSuppression(merged, properties.getTopN());
    }

    /**
     * Walks the ranked list and selects up to {@code topN} entries while
     * suppressing single-word entries whose word also appears in any phrase
     * already accepted into the result. This holds even if the single word
     * outranks the phrase — when the phrase later joins the result, the
     * earlier single is removed and we keep going so the result still hits
     * topN where possible.
     */
    private static List<WordFrequency> takeTopWithPhraseSuppression(List<WordFrequency> ranked, int topN) {
        List<WordFrequency> result = new ArrayList<>(Math.min(topN, ranked.size()));
        Set<String> suppressedSingles = new HashSet<>();
        for (WordFrequency wf : ranked) {
            if (result.size() >= topN) {
                break;
            }
            if (wf.phrase()) {
                result.add(wf);
                for (String w : wf.text().split(" ")) {
                    suppressedSingles.add(w);
                }
                result.removeIf(prev -> !prev.phrase() && suppressedSingles.contains(prev.text()));
            } else if (!suppressedSingles.contains(wf.text())) {
                result.add(wf);
            }
        }
        return result;
    }

    List<String> tokenize(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String plain = Jsoup.parse(raw).text();
        String[] parts = WORD_SPLIT.split(plain.toLowerCase(Locale.ROOT));
        List<String> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            String cleaned = LEADING_TRAILING_APOSTROPHES.matcher(part).replaceAll("");
            if (!cleaned.isEmpty()) {
                out.add(cleaned);
            }
        }
        return out;
    }

    private boolean shouldKeep(String token) {
        if (token.length() < properties.getMinTokenLength()) {
            return false;
        }
        if (stopWordFilter.isStopWord(token)) {
            return false;
        }
        // Purely numeric tokens rarely add signal.
        return !token.chars().allMatch(Character::isDigit);
    }

    private void countBigrams(List<String> tokens, Map<String, Integer> phraseCounts) {
        for (int i = 0; i < tokens.size() - 1; i++) {
            String a = tokens.get(i);
            String b = tokens.get(i + 1);
            if (a.equals(b)) {
                continue;
            }
            String phrase = a + " " + b;
            phraseCounts.merge(phrase, 1, Integer::sum);
        }
    }
}
