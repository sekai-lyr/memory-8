package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.util.List;

public record ProExamSubmitResponse(
        int totalCount,
        int correctCount,
        int wrongCount,
        BigDecimal accuracy,
        int score,
        String levelName,
        String recommendation,
        List<String> weakPoints,
        List<ProExamQuestionResultResponse> results
) {
}
