package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.util.List;

public record MembershipPlanResponse(
        String code,
        String name,
        BigDecimal priceYuan,
        int durationDays,
        List<String> features
) {
}
