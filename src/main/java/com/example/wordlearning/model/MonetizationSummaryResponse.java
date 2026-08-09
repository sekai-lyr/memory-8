package com.example.wordlearning.model;

import java.math.BigDecimal;

public record MonetizationSummaryResponse(
        long totalCodes,
        long activeSubscriptions,
        int totalCapacity,
        int redeemedUses,
        int remainingUses,
        BigDecimal estimatedRevenueYuan
) {
}
