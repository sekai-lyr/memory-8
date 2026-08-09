package com.example.wordlearning.model;

import java.util.List;

public class WordStatsResponse {
    private Long totalWords;
    private List<WordCategoryStatsResponse> categories;

    public WordStatsResponse(Long totalWords, List<WordCategoryStatsResponse> categories) {
        this.totalWords = totalWords;
        this.categories = categories;
    }

    public Long getTotalWords() {
        return totalWords;
    }

    public List<WordCategoryStatsResponse> getCategories() {
        return categories;
    }
}
