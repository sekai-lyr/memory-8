package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.MembershipPlanResponse;
import com.example.wordlearning.model.MembershipResponse;
import com.example.wordlearning.model.RedeemCodeRequest;
import com.example.wordlearning.service.MembershipService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership")
public class MembershipControl extends BaseControl {
    private final MembershipService membershipService;

    public MembershipControl(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public ApiResponse<MembershipResponse> current() {
        return ApiResponse.success(membershipService.getMembership(requireUserId()));
    }

    @GetMapping("/plans")
    public ApiResponse<List<MembershipPlanResponse>> plans() {
        return ApiResponse.success(membershipService.listPlans());
    }

    @PostMapping("/redeem")
    public ApiResponse<MembershipResponse> redeem(@Valid @RequestBody RedeemCodeRequest request) {
        return ApiResponse.success("Redeemed successfully", membershipService.redeem(requireUserId(), request.code()));
    }
}
