package com.example.wordlearning.model;

import java.math.BigDecimal;
import java.util.Map;

public class DailySummaryResponse {
    private Integer totalCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracy;
    private Map<String, Long> statusSummary;
    private String suggestion;

    public DailySummaryResponse(Integer totalCount, Integer correctCount, Integer wrongCount,
                                BigDecimal accuracy, Map<String, Long> statusSummary, String suggestion) {
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.accuracy = accuracy;
        this.statusSummary = statusSummary;
        this.suggestion = suggestion;
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

    public Map<String, Long> getStatusSummary() {
        return statusSummary;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
