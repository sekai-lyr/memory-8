package com.example.wordlearning.model;

import java.time.LocalDateTime;

public record DailyTaskResponse(
        Long id,
        String taskKey,
        String title,
        String detail,
        String route,
        int targetCount,
        boolean completed,
        LocalDateTime completedTime
) {
}
