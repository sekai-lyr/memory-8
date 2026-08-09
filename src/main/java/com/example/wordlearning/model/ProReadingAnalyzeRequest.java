package com.example.wordlearning.model;

public record ProReadingAnalyzeRequest(
        String title,
        String content,
        Integer maxWords
) {
}
