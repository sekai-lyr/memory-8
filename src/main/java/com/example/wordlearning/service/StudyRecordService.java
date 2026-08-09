package com.example.wordlearning.service;

import com.example.wordlearning.model.DailySummaryResponse;
import com.example.wordlearning.model.HeatmapDayResponse;
import com.example.wordlearning.model.StudyRecordRequest;
import com.example.wordlearning.model.StudyRecordResponse;
import java.util.List;

public interface StudyRecordService {
    List<StudyRecordResponse> list(Long userId);

    StudyRecordResponse add(Long userId, StudyRecordRequest request);

    DailySummaryResponse dailySummary(Long userId);

    List<HeatmapDayResponse> heatmap(Long userId, int days);
}

