package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.ProReadingAnalyzeRequest;
import com.example.wordlearning.model.ProReadingImportRequest;
import com.example.wordlearning.model.ProReadingResponse;
import com.example.wordlearning.service.ProReadingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pro-reading")
public class ProReadingControl extends BaseControl {
    private final ProReadingService proReadingService;

    public ProReadingControl(ProReadingService proReadingService) {
        this.proReadingService = proReadingService;
    }

    @PostMapping("/analyze")
    public ApiResponse<ProReadingResponse> analyze(@RequestBody ProReadingAnalyzeRequest request) {
        return ApiResponse.success(proReadingService.analyze(requireUserId(), request));
    }

    @PostMapping("/import")
    public ApiResponse<ImportResultResponse> importWords(@RequestBody ProReadingImportRequest request) {
        return ApiResponse.success(proReadingService.importWords(requireUserId(), request));
    }
}
