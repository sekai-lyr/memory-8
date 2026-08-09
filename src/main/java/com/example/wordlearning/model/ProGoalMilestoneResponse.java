package com.example.wordlearning.model;

public record ProGoalMilestoneResponse(
        String weekLabel,
        int targetWords,
        String focus
) {
}
