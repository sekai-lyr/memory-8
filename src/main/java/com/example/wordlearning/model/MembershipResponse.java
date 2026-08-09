package com.example.wordlearning.model;

import java.time.LocalDateTime;
import java.util.List;

public record MembershipResponse(
        String planType,
        String status,
        boolean pro,
        LocalDateTime expireTime,
        long daysLeft,
        List<String> enabledFeatures,
        List<String> lockedFeatures
) {
}
