package com.example.wordlearning.model;

import java.util.List;

public record ProExamQuestionResponse(
        Long wordId,
        String wordType,
        String english,
        List<String> options,
        String status,
        int wrongCount,
        int unknownCount,
        String sourceReason
) {
}
