package com.example.wordlearning.service;

import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.model.WordResponse;
import java.util.List;

public interface FavoriteWordService {
    List<WordResponse> list(Long userId);

    WordResponse add(Long userId, WordBookRequest request);

    void delete(Long userId, Long id);

    void clear(Long userId);
}

