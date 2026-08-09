package com.example.wordlearning.service.impl;

import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.dataobject.FavoriteWordDO;
import com.example.wordlearning.repository.FavoriteWordRepository;
import com.example.wordlearning.service.FavoriteWordService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FavoriteWordServiceImpl implements FavoriteWordService {
    private final FavoriteWordRepository favoriteWordRepository;

    public FavoriteWordServiceImpl(FavoriteWordRepository favoriteWordRepository) {
        this.favoriteWordRepository = favoriteWordRepository;
    }

    @Override
    public List<WordResponse> list(Long userId) {
        return favoriteWordRepository.findByUserIdOrderByCreatedTimeDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WordResponse add(Long userId, WordBookRequest request) {
        FavoriteWordDO FavoriteWordDO = favoriteWordRepository
                .findByUserIdAndWordIdAndWordType(userId, request.getWordId(), request.getWordType())
                .orElseGet(FavoriteWordDO::new);
        if (FavoriteWordDO.getId() == null) {
            FavoriteWordDO.setUserId(userId);
            FavoriteWordDO.setWordId(request.getWordId());
            FavoriteWordDO.setWordType(request.getWordType());
        }
        FavoriteWordDO.setEnglish(request.getEnglish());
        FavoriteWordDO.setChinese(request.getChinese());
        return toResponse(favoriteWordRepository.save(FavoriteWordDO));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        favoriteWordRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        favoriteWordRepository.deleteByUserId(userId);
    }

    private WordResponse toResponse(FavoriteWordDO WordDO) {
        return new WordResponse(WordDO.getId(), WordDO.getWordId(), WordDO.getEnglish(), WordDO.getChinese(), null, null, null, null, WordDO.getWordType());
    }
}

