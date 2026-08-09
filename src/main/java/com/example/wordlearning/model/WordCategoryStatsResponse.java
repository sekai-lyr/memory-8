package com.example.wordlearning.model;

public class WordCategoryStatsResponse {
    private String category;
    private Long count;

    public WordCategoryStatsResponse(String category, Long count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public Long getCount() {
        return count;
    }
}
