package com.example.wordlearning.model;

public record ProReadingImportWordRequest(
        String english,
        String chinese,
        String phonetic,
        String exampleSentence,
        String category,
        String difficulty
) {
}
