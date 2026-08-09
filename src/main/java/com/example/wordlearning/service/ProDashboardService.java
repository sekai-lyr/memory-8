package com.example.wordlearning.service;

import com.example.wordlearning.model.ProDashboardResponse;
import com.example.wordlearning.model.RandomWordResponse;
import java.util.List;

public interface ProDashboardService {
    ProDashboardResponse dashboard(Long userId);

    List<RandomWordResponse> dictationWords(Long userId, int count);
}
