package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.model.LearningStatusRequest;
import com.example.wordlearning.model.LearningStatusResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.service.LearningStatusService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LearningStatusServiceImpl implements LearningStatusService {
    public static final String NEW = "NEW";
    public static final String LEARNING = "LEARNING";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String HIGH_WRONG = "HIGH_WRONG";
    public static final String MASTERED = "MASTERED";

    private final LearningStatusRepository learningStatusRepository;

    public LearningStatusServiceImpl(LearningStatusRepository learningStatusRepository) {
        this.learningStatusRepository = learningStatusRepository;
    }

    @Override
    public LearningStatusResponse record(Long userId, LearningStatusRequest request) {
        LearningStatusDO statusDO = learningStatusRepository
                .findByUserIdAndWordIdAndWordType(userId, request.getWordId(), request.getWordType())
                .orElseGet(() -> create(userId, request));
        statusDO.setEnglish(request.getEnglish());
        statusDO.setLastPracticedTime(LocalDateTime.now());

        String result = request.getResult().trim().toUpperCase();
        if ("CORRECT".equals(result)) {
            statusDO.setCorrectCount(statusDO.getCorrectCount() + 1);
        } else if ("UNKNOWN".equals(result)) {
            statusDO.setUnknownCount(statusDO.getUnknownCount() + 1);
            statusDO.setWrongCount(statusDO.getWrongCount() + 1);
        } else if ("MASTERED".equals(result)) {
            statusDO.setStatus(MASTERED);
            scheduleReview(statusDO, result);
            return toResponse(learningStatusRepository.save(statusDO));
        } else {
            statusDO.setWrongCount(statusDO.getWrongCount() + 1);
        }

        statusDO.setStatus(decideStatus(statusDO));
        scheduleReview(statusDO, result);
        return toResponse(learningStatusRepository.save(statusDO));
    }

    @Override
    public Map<String, Long> summary(Long userId) {
        Map<String, Long> summary = new HashMap<>();
        summary.put(NEW, 0L);
        summary.put(LEARNING, 0L);
        summary.put(UNKNOWN, 0L);
        summary.put(HIGH_WRONG, 0L);
        summary.put(MASTERED, 0L);
        summary.put("DUE", learningStatusRepository
                .countByUserIdAndNextReviewTimeLessThanEqualAndStatusNot(userId, LocalDateTime.now(), MASTERED));
        learningStatusRepository.countByStatus(userId)
                .forEach(row -> summary.put((String) row[0], (Long) row[1]));
        return summary;
    }

    @Override
    public List<LearningStatusResponse> list(Long userId) {
        return learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private LearningStatusDO create(Long userId, LearningStatusRequest request) {
        LearningStatusDO statusDO = new LearningStatusDO();
        statusDO.setUserId(userId);
        statusDO.setWordId(request.getWordId());
        statusDO.setWordType(request.getWordType());
        statusDO.setEnglish(request.getEnglish());
        statusDO.setStatus(NEW);
        statusDO.setCorrectCount(0);
        statusDO.setWrongCount(0);
        statusDO.setUnknownCount(0);
        statusDO.setReviewIntervalDays(0);
        statusDO.setNextReviewTime(LocalDateTime.now());
        return statusDO;
    }

    private String decideStatus(LearningStatusDO statusDO) {
        if (MASTERED.equals(statusDO.getStatus())) {
            return MASTERED;
        }
        if (statusDO.getWrongCount() >= 3) {
            return HIGH_WRONG;
        }
        if (statusDO.getUnknownCount() > 0) {
            return UNKNOWN;
        }
        return LEARNING;
    }

    private void scheduleReview(LearningStatusDO statusDO, String result) {
        LocalDateTime now = LocalDateTime.now();
        int nextInterval;
        if (MASTERED.equals(result)) {
            nextInterval = 30;
        } else if ("CORRECT".equals(result)) {
            int current = statusDO.getReviewIntervalDays() == null || statusDO.getReviewIntervalDays() < 1
                    ? 1
                    : statusDO.getReviewIntervalDays();
            nextInterval = statusDO.getCorrectCount() <= 1 ? 1 : Math.min(current * 2, 60);
        } else {
            nextInterval = 1;
        }
        statusDO.setReviewIntervalDays(nextInterval);
        statusDO.setNextReviewTime(now.plusDays(nextInterval));
    }

    private LearningStatusResponse toResponse(LearningStatusDO statusDO) {
        return new LearningStatusResponse(statusDO.getWordId(), statusDO.getWordType(), statusDO.getEnglish(),
                statusDO.getStatus(), statusDO.getCorrectCount(), statusDO.getWrongCount(), statusDO.getUnknownCount(),
                statusDO.getReviewIntervalDays(), statusDO.getNextReviewTime());
    }
}
