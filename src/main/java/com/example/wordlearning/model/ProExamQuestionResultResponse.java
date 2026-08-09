package com.example.wordlearning.model;

public record ProExamQuestionResultResponse(
        Long wordId,
        String wordType,
        String english,
        String selectedAnswer,
        String correctAnswer,
        boolean correct,
        String diagnosis
) {
}
