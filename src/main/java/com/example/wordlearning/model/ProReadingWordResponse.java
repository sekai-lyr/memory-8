package com.example.wordlearning.model;

public record ProReadingWordResponse(
        Long wordId,
        String english,
        String chinese,
        String phonetic,
        String exampleSentence,
        String sourceCategory,
        String difficulty,
        int frequency,
        boolean alreadyLearning,
        boolean alreadyInUserWords,
        boolean mastered,
        int priorityScore,
        String reason
) {
}
