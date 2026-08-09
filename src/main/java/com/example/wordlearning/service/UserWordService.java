package com.example.wordlearning.service;

import com.example.wordlearning.model.UserWordRequest;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.WordResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface UserWordService {
    List<WordResponse> list(Long userId, String keyword);

    WordResponse add(Long userId, UserWordRequest request);

    WordResponse update(Long userId, Long id, UserWordRequest request);

    void delete(Long userId, Long id);

    ImportResultResponse importCsv(Long userId, MultipartFile file) throws IOException;
}

