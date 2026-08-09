package com.example.wordlearning.service.impl;

import com.example.wordlearning.model.StudyRecordRequest;
import com.example.wordlearning.model.StudyRecordResponse;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.model.DailySummaryResponse;
import com.example.wordlearning.model.HeatmapDayResponse;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.service.LearningStatusService;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.StudyRecordService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordRepository studyRecordRepository;
    private final LearningStatusService learningStatusService;
    private final MembershipService membershipService;

    public StudyRecordServiceImpl(StudyRecordRepository studyRecordRepository, LearningStatusService learningStatusService,
                                  MembershipService membershipService) {
        this.studyRecordRepository = studyRecordRepository;
        this.learningStatusService = learningStatusService;
        this.membershipService = membershipService;
    }

    @Override
    public List<StudyRecordResponse> list(Long userId) {
        return studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StudyRecordResponse add(Long userId, StudyRecordRequest request) {
        StudyRecordDO record = new StudyRecordDO();
        record.setUserId(userId);
        record.setTotalCount(request.getTotalCount());
        record.setCorrectCount(request.getCorrectCount());
        record.setWrongCount(request.getWrongCount());
        BigDecimal accuracy = request.getAccuracy();
        if (accuracy == null && request.getTotalCount() != null && request.getTotalCount() > 0) {
            accuracy = BigDecimal.valueOf(request.getCorrectCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(request.getTotalCount()), 2, RoundingMode.HALF_UP);
        }
        record.setAccuracy(accuracy == null ? BigDecimal.ZERO : accuracy);
        record.setMode(request.getMode() == null ? "normal" : request.getMode());
        return toResponse(studyRecordRepository.save(record));
    }

    @Override
    public DailySummaryResponse dailySummary(Long userId) {
        membershipService.requirePro(userId, "学习分析");
        LocalDate today = LocalDate.now();
        List<StudyRecordDO> todayRecords = studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId).stream()
                .filter(item -> item.getCreatedTime() != null && item.getCreatedTime().toLocalDate().equals(today))
                .toList();
        int total = todayRecords.stream().mapToInt(item -> value(item.getTotalCount())).sum();
        int correct = todayRecords.stream().mapToInt(item -> value(item.getCorrectCount())).sum();
        int wrong = todayRecords.stream().mapToInt(item -> value(item.getWrongCount())).sum();
        BigDecimal accuracy = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(correct).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        Map<String, Long> statusSummary = learningStatusService.summary(userId);
        String suggestion = wrong >= correct && total > 0
                ? "今天错题偏多，建议下一轮使用智能复习队列。"
                : "今天状态不错，可以继续推进新词或复习高频错误词。";
        if (total == 0) {
            suggestion = "今天还没有完成练习，建议先做一轮 10 题热身。";
        }
        return new DailySummaryResponse(total, correct, wrong, accuracy, statusSummary, suggestion);
    }

    @Override
    public List<HeatmapDayResponse> heatmap(Long userId, int days) {
        membershipService.requirePro(userId, "学习热力图");
        int safeDays = Math.min(120, Math.max(7, days));
        LocalDate start = LocalDate.now().minusDays(safeDays - 1L);
        List<StudyRecordDO> records = studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId);
        List<HeatmapDayResponse> result = new ArrayList<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = start.plusDays(i);
            int total = records.stream()
                    .filter(item -> item.getCreatedTime() != null && item.getCreatedTime().toLocalDate().equals(date))
                    .mapToInt(item -> value(item.getTotalCount()))
                    .sum();
            int correct = records.stream()
                    .filter(item -> item.getCreatedTime() != null && item.getCreatedTime().toLocalDate().equals(date))
                    .mapToInt(item -> value(item.getCorrectCount()))
                    .sum();
            result.add(new HeatmapDayResponse(date.toString(), total, correct));
        }
        return result;
    }

    private StudyRecordResponse toResponse(StudyRecordDO record) {
        return new StudyRecordResponse(record.getId(), record.getTotalCount(), record.getCorrectCount(),
                record.getWrongCount(), record.getAccuracy(), record.getMode(), record.getCreatedTime());
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}

