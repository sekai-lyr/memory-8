package com.example.wordlearning.model;

import java.util.List;

public record ProReviewResponse(
        String focusTitle,
        String focusDetail,
        String nextAction,
        String recommendedMode,
        int recommendedCount,
        ProReviewMetrics metrics,
        List<ProReviewBucketResponse> buckets,
        List<ProReviewDayResponse> upcomingDays,
        List<ProReviewWordResponse> priorityWords,
        List<String> vipReasons
) {
}
