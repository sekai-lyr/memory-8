package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProGoalResponse(
        boolean hasGoal,
        Long id,
        String targetName,
        LocalDate targetDate,
        int dailyMinutes,
        int targetWordCount,
        int daysLeft,
        int expectedDailyWords,
        int progressWords,
        BigDecimal progressPercent,
        String paceStatus,
        String recommendation,
        ProGoalMetricsResponse metrics,
        List<ProGoalMissionResponse> todayMissions,
        List<ProGoalMilestoneResponse> weeklyMilestones
) {
}
