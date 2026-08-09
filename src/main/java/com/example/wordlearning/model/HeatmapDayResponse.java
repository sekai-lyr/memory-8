package com.example.wordlearning.model;

public class HeatmapDayResponse {
    private String date;
    private Integer totalCount;
    private Integer correctCount;

    public HeatmapDayResponse(String date, Integer totalCount, Integer correctCount) {
        this.date = date;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
    }

    public String getDate() {
        return date;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }
}
