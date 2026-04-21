package com.newscloud.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StopWordFilterTest {

    private final StopWordFilter filter = new StopWordFilter();

    @Test
    void recognisesCommonStopWords() {
        assertThat(filter.isStopWord("the")).isTrue();
        assertThat(filter.isStopWord("AND")).isTrue();
        assertThat(filter.isStopWord("a")).isTrue();
        assertThat(filter.isStopWord("this")).isTrue();
    }

    @Test
    void letsContentWordsThrough() {
        assertThat(filter.isStopWord("election")).isFalse();
        assertThat(filter.isStopWord("sydney")).isFalse();
        assertThat(filter.isStopWord("climate")).isFalse();
    }

    @Test
    void handlesNullAndBlank() {
        assertThat(filter.isStopWord(null)).isFalse();
        assertThat(filter.isStopWord("")).isFalse();
    }

    @Test
    void listIsPopulated() {
        assertThat(filter.size()).isGreaterThan(100);
    }

    @Test
    void filtersSubstringFragments() {
        assertThat(filter.isStopWord("australia")).isTrue();
        assertThat(filter.isStopWord("australian")).isTrue();
        assertThat(filter.isStopWord("podcast")).isTrue();
        assertThat(filter.isStopWord("podcasts")).isTrue();
        assertThat(filter.isStopWord("liveblog")).isTrue();
        assertThat(filter.isStopWord("blogger")).isTrue();
        assertThat(filter.isStopWord("readers")).isTrue();
    }
}
