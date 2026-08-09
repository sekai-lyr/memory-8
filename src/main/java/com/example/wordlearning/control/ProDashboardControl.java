package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ProDashboardResponse;
import com.example.wordlearning.model.RandomWordResponse;
import com.example.wordlearning.service.ProDashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pro")
public class ProDashboardControl extends BaseControl {
    private final ProDashboardService proDashboardService;

    public ProDashboardControl(ProDashboardService proDashboardService) {
        this.proDashboardService = proDashboardService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<ProDashboardResponse> dashboard() {
        return ApiResponse.success(proDashboardService.dashboard(requireUserId()));
    }

    @GetMapping("/dictation")
    public ApiResponse<List<RandomWordResponse>> dictation(@RequestParam(defaultValue = "12") int count) {
        return ApiResponse.success(proDashboardService.dictationWords(requireUserId(), count));
    }
}
