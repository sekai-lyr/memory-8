package com.example.wordlearning.model;

import java.util.List;

public record ProReadingResponse(
        String title,
        int totalTokens,
        int uniqueWords,
        int matchedWords,
        int suggestedWords,
        int knownWords,
        int difficultyScore,
        String levelName,
        String summary,
        List<ProReadingWordResponse> words,
        List<String> highlights
) {
}
