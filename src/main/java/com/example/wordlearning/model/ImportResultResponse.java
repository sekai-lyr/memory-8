package com.example.wordlearning.model;

public class ImportResultResponse {
    private int successCount;
    private int skippedCount;
    private String message;

    public ImportResultResponse(int successCount, int skippedCount, String message) {
        this.successCount = successCount;
        this.skippedCount = skippedCount;
        this.message = message;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public String getMessage() {
        return message;
    }
}

