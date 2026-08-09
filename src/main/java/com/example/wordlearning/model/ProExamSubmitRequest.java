package com.example.wordlearning.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProExamSubmitRequest(
        Integer durationSeconds,
        @Valid
        @NotEmpty(message = "答案不能为空")
        List<ProExamAnswerRequest> answers
) {
}
