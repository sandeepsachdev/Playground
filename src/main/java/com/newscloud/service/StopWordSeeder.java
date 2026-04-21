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
 * On first startup (empty table) seeds the {@code stop_words} table from the
 * bundled {@code stopwords.txt} classpath resource. Lines prefixed with
 * {@code ~} are inserted as fragment matches.
 */
@Configuration
public class StopWordSeeder {

    private static final Logger log = LoggerFactory.getLogger(StopWordSeeder.class);

    @Bean
    ApplicationRunner seedStopWords(StopWordRepository repository, StopWordFilter filter) {
        return args -> {
            if (repository.count() > 0) {
                filter.reload();
                return;
            }
            List<StopWord> rows = readSeedResource("stopwords.txt");
            if (rows.isEmpty()) {
                return;
            }
            repository.saveAll(rows);
            filter.reload();
            log.info("Seeded {} stop words from stopwords.txt", rows.size());
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
