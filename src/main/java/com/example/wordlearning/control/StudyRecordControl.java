package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.DailySummaryResponse;
import com.example.wordlearning.model.HeatmapDayResponse;
import com.example.wordlearning.model.StudyRecordRequest;
import com.example.wordlearning.model.StudyRecordResponse;
import com.example.wordlearning.service.StudyRecordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-records")
public class StudyRecordControl extends BaseControl {
    private final StudyRecordService studyRecordService;

    public StudyRecordControl(StudyRecordService studyRecordService) {
        this.studyRecordService = studyRecordService;
    }

    @GetMapping
    public ApiResponse<List<StudyRecordResponse>> list() {
        return ApiResponse.success(studyRecordService.list(requireUserId()));
    }

    @PostMapping
    public ApiResponse<StudyRecordResponse> add(@Valid @RequestBody StudyRecordRequest request) {
        return ApiResponse.success(studyRecordService.add(requireUserId(), request));
    }

    @GetMapping("/daily-summary")
    public ApiResponse<DailySummaryResponse> dailySummary() {
        return ApiResponse.success(studyRecordService.dailySummary(requireUserId()));
    }

    @GetMapping("/heatmap")
    public ApiResponse<List<HeatmapDayResponse>> heatmap(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(studyRecordService.heatmap(requireUserId(), days));
    }
}

