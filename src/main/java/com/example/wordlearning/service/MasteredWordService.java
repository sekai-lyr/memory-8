package com.example.wordlearning.service;

import com.example.wordlearning.model.WordBookRequest;
import java.util.List;

public interface MasteredWordService {
    void add(Long userId, WordBookRequest request);

    List<String> keys(Long userId);

    void delete(Long userId, Long wordId, String wordType);
}
