package com.example.wordlearning.service;

import com.example.wordlearning.model.ProExamPaperResponse;
import com.example.wordlearning.model.ProExamSubmitRequest;
import com.example.wordlearning.model.ProExamSubmitResponse;

public interface ProExamService {
    ProExamPaperResponse paper(Long userId, int count);

    ProExamSubmitResponse submit(Long userId, ProExamSubmitRequest request);
}
