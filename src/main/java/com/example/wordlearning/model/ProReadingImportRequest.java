package com.example.wordlearning.model;

import java.util.List;

public record ProReadingImportRequest(
        String category,
        List<ProReadingImportWordRequest> words
) {
}
