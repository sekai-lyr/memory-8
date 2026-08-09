package com.example.wordlearning.service;

import com.example.wordlearning.model.CheckAnswerRequest;
import com.example.wordlearning.model.CheckAnswerResponse;
import com.example.wordlearning.model.RandomWordResponse;
import com.example.wordlearning.model.WordDetailResponse;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.model.WordStatsResponse;
import java.util.List;

public interface WordService {
    RandomWordResponse randomWord(String category);

    List<RandomWordResponse> randomWords(int count, String category);

    List<RandomWordResponse> smartQueue(Long userId, int count);

    CheckAnswerResponse checkAnswer(CheckAnswerRequest request);

    List<WordResponse> search(String keyword, Long userId, int limit);

    List<WordResponse> list(String category, int limit);

    List<String> categories();

    WordStatsResponse stats();

    WordDetailResponse detail(Long userId, Long wordId, String wordType);
}

