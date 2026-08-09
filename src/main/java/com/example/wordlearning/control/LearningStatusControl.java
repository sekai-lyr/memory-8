package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.LearningStatusRequest;
import com.example.wordlearning.model.LearningStatusResponse;
import com.example.wordlearning.service.LearningStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning-status")
public class LearningStatusControl extends BaseControl {
    private final LearningStatusService learningStatusService;

    public LearningStatusControl(LearningStatusService learningStatusService) {
        this.learningStatusService = learningStatusService;
    }

    @PostMapping
    public ApiResponse<LearningStatusResponse> record(@Valid @RequestBody LearningStatusRequest request) {
        return ApiResponse.success(learningStatusService.record(requireUserId(), request));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.success(learningStatusService.summary(requireUserId()));
    }

    @GetMapping
    public ApiResponse<List<LearningStatusResponse>> list() {
        return ApiResponse.success(learningStatusService.list(requireUserId()));
    }
}
