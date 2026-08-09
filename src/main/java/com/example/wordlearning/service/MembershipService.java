package com.example.wordlearning.service;

import com.example.wordlearning.model.ActivationCodeRequest;
import com.example.wordlearning.model.ActivationCodeResponse;
import com.example.wordlearning.model.MembershipPlanResponse;
import com.example.wordlearning.model.MembershipResponse;
import com.example.wordlearning.model.MonetizationSummaryResponse;
import java.util.List;

public interface MembershipService {
    MembershipResponse getMembership(Long userId);

    List<MembershipPlanResponse> listPlans();

    MembershipResponse redeem(Long userId, String code);

    boolean isPro(Long userId);

    void requirePro(Long userId, String featureName);

    ActivationCodeResponse createActivationCode(String adminSecret, ActivationCodeRequest request);

    List<ActivationCodeResponse> listActivationCodes(String adminSecret);

    MonetizationSummaryResponse getMonetizationSummary(String adminSecret);
}
