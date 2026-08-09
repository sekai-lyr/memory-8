package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.MasteredWordDO;
import com.example.wordlearning.model.LearningStatusRequest;
import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.repository.MasteredWordRepository;
import com.example.wordlearning.service.LearningStatusService;
import com.example.wordlearning.service.MasteredWordService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MasteredWordServiceImpl implements MasteredWordService {
    private final MasteredWordRepository masteredWordRepository;
    private final LearningStatusService learningStatusService;

    public MasteredWordServiceImpl(MasteredWordRepository masteredWordRepository, LearningStatusService learningStatusService) {
        this.masteredWordRepository = masteredWordRepository;
        this.learningStatusService = learningStatusService;
    }

    @Override
    public void add(Long userId, WordBookRequest request) {
        masteredWordRepository.findByUserIdAndWordIdAndWordType(userId, request.getWordId(), request.getWordType())
                .orElseGet(() -> {
                    MasteredWordDO wordDO = new MasteredWordDO();
                    wordDO.setUserId(userId);
                    wordDO.setWordId(request.getWordId());
                    wordDO.setWordType(request.getWordType());
                    wordDO.setEnglish(request.getEnglish());
                    return masteredWordRepository.save(wordDO);
                });
        LearningStatusRequest statusRequest = new LearningStatusRequest();
        statusRequest.setWordId(request.getWordId());
        statusRequest.setWordType(request.getWordType());
        statusRequest.setEnglish(request.getEnglish());
        statusRequest.setResult("MASTERED");
        learningStatusService.record(userId, statusRequest);
    }

    @Override
    public List<String> keys(Long userId) {
        return masteredWordRepository.findByUserId(userId).stream()
                .map(item -> item.getWordType() + ":" + item.getWordId())
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId, Long wordId, String wordType) {
        masteredWordRepository.deleteByUserIdAndWordIdAndWordType(userId, wordId, wordType);
    }
}
