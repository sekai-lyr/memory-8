package com.example.wordlearning.model;

public record ProGoalMetricsResponse(
        int dueReviews,
        int weakWords,
        int learningWords,
        int masteredWords,
        int customWords,
        int practicedSevenDays,
        long availableWords
) {
}
