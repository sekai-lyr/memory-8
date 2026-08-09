package com.example.wordlearning.model;

public record ProReviewMetrics(
        int totalTracked,
        int dueReviews,
        int overdueReviews,
        int highWrong,
        int unknown,
        int learning,
        int mastered,
        int weakWords,
        int futureSevenDayReviews,
        int estimatedMinutes
) {
}
