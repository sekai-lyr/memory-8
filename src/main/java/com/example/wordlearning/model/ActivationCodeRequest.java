package com.example.wordlearning.model;

import java.math.BigDecimal;

public record ActivationCodeRequest(
        String code,
        Integer durationDays,
        Integer maxUses,
        BigDecimal priceYuan,
        String note
) {
}
