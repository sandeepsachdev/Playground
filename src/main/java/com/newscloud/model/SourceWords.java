package com.newscloud.model;

import java.util.List;

public record SourceWords(String source, List<WordFrequency> words) {
}
