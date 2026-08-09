package com.example.wordlearning.control;

import com.example.wordlearning.model.ActivationCodeRequest;
import com.example.wordlearning.model.ActivationCodeResponse;
import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.MonetizationSummaryResponse;
import com.example.wordlearning.service.MembershipService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activation-codes")
public class AdminActivationCodeControl {
    private final MembershipService membershipService;

    public AdminActivationCodeControl(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public ApiResponse<List<ActivationCodeResponse>> list(@RequestHeader("X-Admin-Secret") String adminSecret) {
        return ApiResponse.success(membershipService.listActivationCodes(adminSecret));
    }

    @GetMapping("/summary")
    public ApiResponse<MonetizationSummaryResponse> summary(@RequestHeader("X-Admin-Secret") String adminSecret) {
        return ApiResponse.success(membershipService.getMonetizationSummary(adminSecret));
    }

    @PostMapping
    public ApiResponse<ActivationCodeResponse> create(@RequestHeader("X-Admin-Secret") String adminSecret,
                                                      @RequestBody ActivationCodeRequest request) {
        return ApiResponse.success(membershipService.createActivationCode(adminSecret, request));
    }
}
