package com.example.wordlearning.dataobject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "sekai_word_activation_redemptions")
public class ActivationRedemptionDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_id", nullable = false)
    private Long codeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_type", nullable = false, length = 30)
    private String planType;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "redeemed_time")
    private LocalDateTime redeemedTime;

    @PrePersist
    public void prePersist() {
        redeemedTime = LocalDateTime.now();
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
}
