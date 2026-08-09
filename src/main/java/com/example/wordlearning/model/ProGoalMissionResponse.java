package com.example.wordlearning.model;

public record ProGoalMissionResponse(
        String title,
        int targetCount,
        String route,
        String detail
) {
}
