package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ProGoalRequest;
import com.example.wordlearning.model.ProGoalResponse;
import com.example.wordlearning.service.ProGoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pro-goal")
public class ProGoalControl extends BaseControl {
    private final ProGoalService proGoalService;

    public ProGoalControl(ProGoalService proGoalService) {
        this.proGoalService = proGoalService;
    }

    @GetMapping
    public ApiResponse<ProGoalResponse> current() {
        return ApiResponse.success(proGoalService.current(requireUserId()));
    }

    @PostMapping
    public ApiResponse<ProGoalResponse> save(@Valid @RequestBody ProGoalRequest request) {
        return ApiResponse.success("目标计划已保存", proGoalService.save(requireUserId(), request));
    }
}
