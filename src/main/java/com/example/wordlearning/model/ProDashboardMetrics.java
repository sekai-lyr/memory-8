package com.example.wordlearning.model;

import java.math.BigDecimal;

public record ProDashboardMetrics(
        int dueReviews,
        int highWrong,
        int unknown,
        int learning,
        int mastered,
        int wrongWords,
        int customWords,
        int favoriteWords,
        int sevenDayTotal,
        BigDecimal averageAccuracy
) {
}
