package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StudyRecordResponse {
    private Long id;
    private Integer totalCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracy;
    private String mode;
    private LocalDateTime createdTime;

    public StudyRecordResponse(Long id, Integer totalCount, Integer correctCount, Integer wrongCount,
                               BigDecimal accuracy, String mode, LocalDateTime createdTime) {
        this.id = id;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.accuracy = accuracy;
        this.mode = mode;
        this.createdTime = createdTime;
    }

    public Long getId() {
        return id;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public String getMode() {
        return mode;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}

