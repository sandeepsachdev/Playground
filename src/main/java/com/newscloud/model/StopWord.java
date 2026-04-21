package com.newscloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stop_words", uniqueConstraints = @UniqueConstraint(columnNames = "word"))
public class StopWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String word;

    @Column(nullable = false)
    private boolean fragment;

    protected StopWord() {
    }

    public StopWord(String word, boolean fragment) {
        this.word = word;
        this.fragment = fragment;
    }

    public Long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public boolean isFragment() {
        return fragment;
    }
}
