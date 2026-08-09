package com.example.wordlearning.dataobject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "sekai_word_learning_status")
public class LearningStatusDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Column(name = "word_type", nullable = false, length = 20)
    private String wordType;

    @Column(nullable = false, length = 100)
    private String english;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "unknown_count")
    private Integer unknownCount;

    @Column(name = "review_interval_days")
    private Integer reviewIntervalDays;

    @Column(name = "next_review_time")
    private LocalDateTime nextReviewTime;

    @Column(name = "last_practiced_time")
    private LocalDateTime lastPracticedTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    public void prePersist() {
        createdTime = LocalDateTime.now();
        updatedTime = createdTime;
        lastPracticedTime = createdTime;
        if (correctCount == null) correctCount = 0;
        if (wrongCount == null) wrongCount = 0;
        if (unknownCount == null) unknownCount = 0;
        if (reviewIntervalDays == null) reviewIntervalDays = 0;
        if (nextReviewTime == null) nextReviewTime = createdTime;
    }

    @PreUpdate
    public void preUpdate() {
        updatedTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(Integer unknownCount) {
        this.unknownCount = unknownCount;
    }

    public Integer getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public void setReviewIntervalDays(Integer reviewIntervalDays) {
        this.reviewIntervalDays = reviewIntervalDays;
    }

    public LocalDateTime getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(LocalDateTime nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public LocalDateTime getLastPracticedTime() {
        return lastPracticedTime;
    }

    public void setLastPracticedTime(LocalDateTime lastPracticedTime) {
        this.lastPracticedTime = lastPracticedTime;
    }
}
