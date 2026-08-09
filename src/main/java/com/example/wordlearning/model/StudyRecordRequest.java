package com.example.wordlearning.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class StudyRecordRequest {
    @NotNull(message = "总题数不能为空")
    private Integer totalCount;

    @NotNull(message = "正确数不能为空")
    private Integer correctCount;

    @NotNull(message = "错误数不能为空")
    private Integer wrongCount;

    private BigDecimal accuracy;
    private String mode;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
