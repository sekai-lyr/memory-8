package com.example.wordlearning.model;

import java.util.List;

public record ProDashboardResponse(
        String levelName,
        int retentionScore,
        int dailyTarget,
        String focusTitle,
        String focusDetail,
        String recommendedMode,
        String nextAction,
        ProDashboardMetrics metrics,
        List<ProPlanDayResponse> weeklyPlan,
        List<ProWeakWordResponse> weakWords,
        List<String> privileges
) {
}
