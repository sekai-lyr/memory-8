package com.example.wordlearning.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProExamAnswerRequest(
        @NotNull(message = "单词ID不能为空")
        Long wordId,
        @NotBlank(message = "单词类型不能为空")
        String wordType,
        String selectedAnswer
) {
}
