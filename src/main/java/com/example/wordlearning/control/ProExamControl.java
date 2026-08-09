package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ProExamPaperResponse;
import com.example.wordlearning.model.ProExamSubmitRequest;
import com.example.wordlearning.model.ProExamSubmitResponse;
import com.example.wordlearning.service.ProExamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pro-exam")
public class ProExamControl extends BaseControl {
    private final ProExamService proExamService;

    public ProExamControl(ProExamService proExamService) {
        this.proExamService = proExamService;
    }

    @GetMapping("/paper")
    public ApiResponse<ProExamPaperResponse> paper(@RequestParam(defaultValue = "20") int count) {
        return ApiResponse.success(proExamService.paper(requireUserId(), count));
    }

    @PostMapping("/submit")
    public ApiResponse<ProExamSubmitResponse> submit(@Valid @RequestBody ProExamSubmitRequest request) {
        return ApiResponse.success(proExamService.submit(requireUserId(), request));
    }
}
