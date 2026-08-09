package com.example.wordlearning.model;

import java.time.LocalDateTime;

public class LearningStatusResponse {
    private Long wordId;
    private String wordType;
    private String english;
    private String status;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unknownCount;
    private Integer reviewIntervalDays;
    private LocalDateTime nextReviewTime;

    public LearningStatusResponse(Long wordId, String wordType, String english, String status,
                                  Integer correctCount, Integer wrongCount, Integer unknownCount,
                                  Integer reviewIntervalDays, LocalDateTime nextReviewTime) {
        this.wordId = wordId;
        this.wordType = wordType;
        this.english = english;
        this.status = status;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.unknownCount = unknownCount;
        this.reviewIntervalDays = reviewIntervalDays;
        this.nextReviewTime = nextReviewTime;
    }

    public Long getWordId() {
        return wordId;
    }

    public String getWordType() {
        return wordType;
    }

    public String getEnglish() {
        return english;
    }

    public String getStatus() {
        return status;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public Integer getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public LocalDateTime getNextReviewTime() {
        return nextReviewTime;
    }
}
