package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.exception.PremiumRequiredException;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.UserWordRequest;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.UserWordService;
import com.example.wordlearning.util.CsvImportUtil;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserWordServiceImpl implements UserWordService {
    private static final int FREE_CUSTOM_WORD_LIMIT = 20;

    private final UserWordRepository userWordRepository;
    private final MembershipService membershipService;

    public UserWordServiceImpl(UserWordRepository userWordRepository, MembershipService membershipService) {
        this.userWordRepository = userWordRepository;
        this.membershipService = membershipService;
    }

    @Override
    public List<WordResponse> list(Long userId, String keyword) {
        if (keyword != null && !keyword.trim().isBlank()) {
            String trimmed = keyword.trim();
            return userWordRepository
                    .findByUserIdAndEnglishIgnoreCaseContainingOrUserIdAndChineseContaining(userId, trimmed, userId, trimmed)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return userWordRepository.findByUserIdOrderByIdDesc(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public WordResponse add(Long userId, UserWordRequest request) {
        validate(request);
        if (!membershipService.isPro(userId) && userWordRepository.countByUserId(userId) >= FREE_CUSTOM_WORD_LIMIT) {
            throw new PremiumRequiredException("普通用户最多保存 20 个自定义单词，更多数量需要开通会员");
        }
        String english = request.getEnglish().trim();
        if (userWordRepository.existsByUserIdAndEnglishIgnoreCase(userId, english)) {
            throw new IllegalArgumentException("该单词已经存在");
        }
        UserWordDO wordDO = new UserWordDO();
        fill(wordDO, userId, request);
        return toResponse(userWordRepository.save(wordDO));
    }

    @Override
    public WordResponse update(Long userId, Long id, UserWordRequest request) {
        validate(request);
        UserWordDO wordDO = userWordRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在"));
        fill(wordDO, userId, request);
        return toResponse(userWordRepository.save(wordDO));
    }

    @Override
    public void delete(Long userId, Long id) {
        UserWordDO wordDO = userWordRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在"));
        userWordRepository.delete(wordDO);
    }

    @Override
    public ImportResultResponse importCsv(Long userId, MultipartFile file) throws IOException {
        membershipService.requirePro(userId, "表格批量导入");
        List<UserWordRequest> rows = CsvImportUtil.readUserWords(file);
        int success = 0;
        int skipped = 0;
        for (UserWordRequest row : rows) {
            if (row.getEnglish() == null || row.getEnglish().isBlank()
                    || row.getChinese() == null || row.getChinese().isBlank()
                    || userWordRepository.existsByUserIdAndEnglishIgnoreCase(userId, row.getEnglish().trim())) {
                skipped++;
                continue;
            }
            UserWordDO wordDO = new UserWordDO();
            fill(wordDO, userId, row);
            userWordRepository.save(wordDO);
            success++;
        }
        return new ImportResultResponse(success, skipped,
                "导入完成。模板：english,chinese,phonetic,exampleSentence,category,difficulty");
    }

    private void validate(UserWordRequest request) {
        if (request.getEnglish() == null || request.getEnglish().trim().isBlank()) {
            throw new IllegalArgumentException("英文不能为空");
        }
        if (request.getChinese() == null || request.getChinese().trim().isBlank()) {
            throw new IllegalArgumentException("中文释义不能为空");
        }
    }

    private void fill(UserWordDO wordDO, Long userId, UserWordRequest request) {
        wordDO.setUserId(userId);
        wordDO.setEnglish(request.getEnglish().trim());
        wordDO.setChinese(request.getChinese().trim());
        wordDO.setPhonetic(clean(request.getPhonetic()));
        wordDO.setExampleSentence(clean(request.getExampleSentence()));
        wordDO.setCategory(clean(request.getCategory()));
        wordDO.setDifficulty(clean(request.getDifficulty()));
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private WordResponse toResponse(UserWordDO wordDO) {
        return new WordResponse(wordDO.getId(), wordDO.getEnglish(), wordDO.getChinese(), wordDO.getPhonetic(),
                wordDO.getExampleSentence(), wordDO.getCategory(), wordDO.getDifficulty(), "user");
    }
}
