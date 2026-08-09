package com.example.wordlearning.service;

import com.example.wordlearning.model.LearningStatusRequest;
import com.example.wordlearning.model.LearningStatusResponse;
import java.util.List;
import java.util.Map;

public interface LearningStatusService {
    LearningStatusResponse record(Long userId, LearningStatusRequest request);

    Map<String, Long> summary(Long userId);

    List<LearningStatusResponse> list(Long userId);
}
