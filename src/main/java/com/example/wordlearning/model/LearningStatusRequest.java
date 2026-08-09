package com.example.wordlearning.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LearningStatusRequest {
    @NotNull(message = "单词ID不能为空")
    private Long wordId;

    @NotBlank(message = "单词类型不能为空")
    private String wordType;

    @NotBlank(message = "英文不能为空")
    private String english;

    @NotBlank(message = "学习结果不能为空")
    private String result;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
