package com.example.wordlearning.model;

import java.util.List;

public record ProExamPaperResponse(
        String paperId,
        int totalCount,
        int durationMinutes,
        List<ProExamQuestionResponse> questions,
        List<String> rules
) {
}
