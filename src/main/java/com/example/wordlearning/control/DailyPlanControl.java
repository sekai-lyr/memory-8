package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.DailyPlanResponse;
import com.example.wordlearning.model.DailyTaskUpdateRequest;
import com.example.wordlearning.service.DailyPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-plan")
public class DailyPlanControl extends BaseControl {
    private final DailyPlanService dailyPlanService;

    public DailyPlanControl(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping
    public ApiResponse<DailyPlanResponse> today() {
        return ApiResponse.success(dailyPlanService.today(requireUserId()));
    }

    @PostMapping("/tasks/{taskId}")
    public ApiResponse<DailyPlanResponse> updateTask(@PathVariable Long taskId,
                                                     @RequestBody DailyTaskUpdateRequest request) {
        boolean completed = request == null || request.completed() == null || request.completed();
        return ApiResponse.success(dailyPlanService.updateTask(requireUserId(), taskId, completed));
    }
}
