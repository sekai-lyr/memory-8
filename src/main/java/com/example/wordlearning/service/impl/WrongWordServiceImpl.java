package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.WrongWordDO;
import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.WrongWordService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WrongWordServiceImpl implements WrongWordService {
    private final WrongWordRepository wrongWordRepository;

    public WrongWordServiceImpl(WrongWordRepository wrongWordRepository) {
        this.wrongWordRepository = wrongWordRepository;
    }

    @Override
    public List<WordResponse> list(Long userId) {
        return wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WordResponse add(Long userId, WordBookRequest request) {
        WrongWordDO wrongWordDO = wrongWordRepository.findByUserIdAndWordIdAndWordType(userId, request.getWordId(), request.getWordType())
                .orElseGet(WrongWordDO::new);
        if (wrongWordDO.getId() == null) {
            wrongWordDO.setUserId(userId);
            wrongWordDO.setWordId(request.getWordId());
            wrongWordDO.setWordType(request.getWordType());
            wrongWordDO.setWrongCount(0);
            wrongWordDO.setUnknownCount(0);
        }
        wrongWordDO.setEnglish(request.getEnglish());
        wrongWordDO.setChinese(request.getChinese());
        wrongWordDO.setWrongCount(wrongWordDO.getWrongCount() + 1);
        if (Boolean.TRUE.equals(request.getUnknown())) {
            wrongWordDO.setUnknownCount(wrongWordDO.getUnknownCount() + 1);
        }
        wrongWordDO.setLastWrongTime(LocalDateTime.now());
        return toResponse(wrongWordRepository.save(wrongWordDO));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        wrongWordRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        wrongWordRepository.deleteByUserId(userId);
    }

    private WordResponse toResponse(WrongWordDO wordDO) {
        WordResponse response = new WordResponse(wordDO.getId(), wordDO.getWordId(), wordDO.getEnglish(), wordDO.getChinese(),
                null, null, null, null, wordDO.getWordType());
        response.setWrongCount(wordDO.getWrongCount());
        response.setUnknownCount(wordDO.getUnknownCount());
        return response;
    }
}
