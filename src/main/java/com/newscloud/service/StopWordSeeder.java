package com.newscloud.service;

import com.newscloud.model.StopWord;
import com.newscloud.repository.StopWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * On every startup, tops up the {@code stop_words} table with any entries
 * from the bundled {@code stopwords.txt} classpath resource that aren't
 * already present. Lines prefixed with {@code ~} are inserted as fragment
 * matches. Rows inserted at runtime via the API are preserved.
 */
@Configuration
public class StopWordSeeder {

    private static final Logger log = LoggerFactory.getLogger(StopWordSeeder.class);

    @Bean
    ApplicationRunner seedStopWords(StopWordRepository repository, StopWordFilter filter) {
        return args -> {
            List<StopWord> rows = readSeedResource("stopwords.txt");
            List<StopWord> missing = new ArrayList<>();
            for (StopWord row : rows) {
                if (!repository.existsByWord(row.getWord())) {
                    missing.add(row);
                }
            }
            if (!missing.isEmpty()) {
                repository.saveAll(missing);
                log.info("Seeded {} new stop words from stopwords.txt", missing.size());
            }
            filter.reload();
        };
    }

    private static List<StopWord> readSeedResource(String resource) {
        Set<String> seen = new HashSet<>();
        List<StopWord> rows = new ArrayList<>();
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
                boolean fragment = false;
                if (lower.startsWith("~")) {
                    fragment = true;
                    lower = lower.substring(1).trim();
                }
                if (lower.isEmpty() || !seen.add(lower)) {
                    continue;
                }
                rows.add(new StopWord(lower, fragment));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read seed stop-word list " + resource, e);
        }
        return rows;
    }
}
