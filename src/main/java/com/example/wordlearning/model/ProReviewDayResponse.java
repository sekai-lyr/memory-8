package com.example.wordlearning.model;

import java.time.LocalDate;

public record ProReviewDayResponse(
        LocalDate date,
        String dayLabel,
        int dueCount,
        String loadLevel,
        String suggestion
) {
}
