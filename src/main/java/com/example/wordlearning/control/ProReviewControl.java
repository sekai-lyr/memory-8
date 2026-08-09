package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ProReviewResponse;
import com.example.wordlearning.service.ProReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pro-review")
public class ProReviewControl extends BaseControl {
    private final ProReviewService proReviewService;

    public ProReviewControl(ProReviewService proReviewService) {
        this.proReviewService = proReviewService;
    }

    @GetMapping
    public ApiResponse<ProReviewResponse> review() {
        return ApiResponse.success(proReviewService.review(requireUserId()));
    }
}
