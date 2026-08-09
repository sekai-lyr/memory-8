package com.example.wordlearning.model;

import java.time.LocalDateTime;

public class RandomWordResponse {
    private Long id;
    private String english;
    private String wordType;
    private Integer wrongCount;
    private Integer unknownCount;
    private String status;
    private Integer reviewIntervalDays;
    private LocalDateTime nextReviewTime;

    public RandomWordResponse(Long id, String english, String wordType) {
        this.id = id;
        this.english = english;
        this.wordType = wordType;
    }

    public Long getId() {
        return id;
    }

    public String getEnglish() {
        return english;
    }

    public String getWordType() {
        return wordType;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(Integer unknownCount) {
        this.unknownCount = unknownCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public void setReviewIntervalDays(Integer reviewIntervalDays) {
        this.reviewIntervalDays = reviewIntervalDays;
    }

    public LocalDateTime getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(LocalDateTime nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }
}

