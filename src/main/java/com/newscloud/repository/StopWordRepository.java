package com.newscloud.repository;

import com.newscloud.model.StopWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StopWordRepository extends JpaRepository<StopWord, Long> {

    Optional<StopWord> findByWord(String word);

    boolean existsByWord(String word);
}
