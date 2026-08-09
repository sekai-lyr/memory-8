package com.example.wordlearning.model;

public record ProReviewBucketResponse(
        String key,
        String title,
        int count,
        String level,
        String detail,
        String route
) {
}
