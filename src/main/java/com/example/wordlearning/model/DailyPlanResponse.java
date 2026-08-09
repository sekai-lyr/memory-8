package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyPlanResponse(
        LocalDate taskDate,
        int totalTasks,
        int completedTasks,
        BigDecimal completionRate,
        int streakDays,
        String suggestion,
        List<DailyTaskResponse> tasks
) {
}
