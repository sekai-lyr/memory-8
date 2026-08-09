package com.example.wordlearning.service;

import com.example.wordlearning.model.DailyPlanResponse;

public interface DailyPlanService {
    DailyPlanResponse today(Long userId);

    DailyPlanResponse updateTask(Long userId, Long taskId, boolean completed);
}
