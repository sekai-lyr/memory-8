package com.example.wordlearning.model;

public record ProPlanDayResponse(
        String dayLabel,
        String title,
        int targetCount,
        String focus
) {
}
