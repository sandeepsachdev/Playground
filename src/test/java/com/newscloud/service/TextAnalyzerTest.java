package com.newscloud.service;

import com.newscloud.config.FeedProperties;
import com.newscloud.model.Article;
import com.newscloud.model.WordFrequency;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextAnalyzerTest {

    private final StopWordFilter stopWords = new StopWordFilter();
    private final FeedProperties properties = defaults();
    private final TextAnalyzer analyzer = new TextAnalyzer(stopWords, properties);

    @Test
    void tokenizeStripsHtmlAndLowercases() {
        List<String> tokens = analyzer.tokenize(
                "<p>Breaking: <b>Sydney</b> Harbour Bridge reopens &amp; crowds cheer!</p>");
        assertThat(tokens).contains("breaking", "sydney", "harbour", "bridge", "reopens", "crowds", "cheer");
    }

    @Test
    void analyzeFiltersStopWordsAndCountsFrequencies() {
        Article a1 = article("Climate summit opens in Sydney", "Leaders gather for the climate summit in Sydney.");
        Article a2 = article("Sydney hosts climate talks", "The climate talks continue in Sydney today.");
        Article a3 = article("Election results delayed", "Counting continues after the election.");

        List<WordFrequency> result = analyzer.analyze(List.of(a1, a2, a3));

        assertThat(result).extracting(WordFrequency::text).doesNotContain("the", "in", "for", "a");
        assertThat(topWord(result, "sydney")).isEqualTo(4);
        assertThat(topWord(result, "climate")).isEqualTo(4);
        assertThat(topWord(result, "election")).isEqualTo(2);
    }

    @Test
    void analyzeProducesBigramPhrasesWhenEnabled() {
        Article a = article("Climate summit Sydney", "Climate summit Sydney climate summit");
        Article b = article("Climate summit in Sydney", "The climate summit kicks off.");

        List<WordFrequency> result = analyzer.analyze(List.of(a, b));

        assertThat(result).anyMatch(w -> w.phrase() && w.text().equals("climate summit"));
        int count = result.stream()
                .filter(w -> w.phrase() && w.text().equals("climate summit"))
                .mapToInt(WordFrequency::count)
                .findFirst()
                .orElse(0);
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void analyzeRespectsTopNLimit() {
        properties.setTopN(3);
        Article a = article("One two three four five",
                "One two three four five six seven eight nine ten eleven twelve");
        List<WordFrequency> result = analyzer.analyze(List.of(a));
        assertThat(result).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void analyzeDropsTokensShorterThanMinLength() {
        properties.setMinTokenLength(5);
        Article a = article("The cat sat on the mat", "Elephant enormous incredibly");
        List<WordFrequency> result = analyzer.analyze(List.of(a));

        assertThat(result).extracting(WordFrequency::text)
                .allSatisfy(t -> assertThat(t.length()).isGreaterThanOrEqualTo(5));
        assertThat(result).extracting(WordFrequency::text).contains("elephant", "enormous", "incredibly");
    }

    private static int topWord(List<WordFrequency> words, String term) {
        return words.stream()
                .filter(w -> !w.phrase() && w.text().equals(term))
                .mapToInt(WordFrequency::count)
                .findFirst()
                .orElse(0);
    }

    private static FeedProperties defaults() {
        FeedProperties p = new FeedProperties();
        p.setTopN(100);
        p.setMinTokenLength(3);
        p.setIncludePhrases(true);
        return p;
    }

    private static Article article(String title, String description) {
        return new Article(title, description, "test", "https://example/" + title.hashCode(), Instant.now());
    }
}
