package com.example.wordlearning.service;

import com.example.wordlearning.model.ProReviewResponse;

public interface ProReviewService {
    ProReviewResponse review(Long userId);
}
