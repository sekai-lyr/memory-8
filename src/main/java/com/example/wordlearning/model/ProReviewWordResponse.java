package com.example.wordlearning.model;

import java.time.LocalDateTime;

public record ProReviewWordResponse(
        Long wordId,
        String wordType,
        String english,
        String status,
        int priorityScore,
        int wrongCount,
        int unknownCount,
        int correctCount,
        int reviewIntervalDays,
        LocalDateTime nextReviewTime,
        String reason
) {
}
