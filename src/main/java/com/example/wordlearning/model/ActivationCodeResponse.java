package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivationCodeResponse(
        String code,
        String planType,
        Integer durationDays,
        Integer maxUses,
        BigDecimal priceYuan,
        Integer usedCount,
        Integer remainingUses,
        BigDecimal redeemedRevenueYuan,
        String status,
        String note,
        LocalDateTime createdTime
) {
}
