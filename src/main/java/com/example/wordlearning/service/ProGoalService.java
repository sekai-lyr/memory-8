package com.example.wordlearning.service;

import com.example.wordlearning.model.ProGoalRequest;
import com.example.wordlearning.model.ProGoalResponse;

public interface ProGoalService {
    ProGoalResponse current(Long userId);

    ProGoalResponse save(Long userId, ProGoalRequest request);
}
