package com.example.wordlearning.service;

import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.ProReadingAnalyzeRequest;
import com.example.wordlearning.model.ProReadingImportRequest;
import com.example.wordlearning.model.ProReadingResponse;

public interface ProReadingService {
    ProReadingResponse analyze(Long userId, ProReadingAnalyzeRequest request);

    ImportResultResponse importWords(Long userId, ProReadingImportRequest request);
}
