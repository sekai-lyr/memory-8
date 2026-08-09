package com.example.wordlearning.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckAnswerRequest {
    @NotNull(message = "单词ID不能为空")
    private Long wordId;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String wordType = "public";

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }
}
