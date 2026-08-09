package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.ActivationCodeDO;
import com.example.wordlearning.dataobject.ActivationRedemptionDO;
import com.example.wordlearning.dataobject.UserSubscriptionDO;
import com.example.wordlearning.exception.PremiumRequiredException;
import com.example.wordlearning.model.ActivationCodeRequest;
import com.example.wordlearning.model.ActivationCodeResponse;
import com.example.wordlearning.model.MembershipPlanResponse;
import com.example.wordlearning.model.MembershipResponse;
import com.example.wordlearning.model.MonetizationSummaryResponse;
import com.example.wordlearning.repository.ActivationCodeRepository;
import com.example.wordlearning.repository.ActivationRedemptionRepository;
import com.example.wordlearning.repository.UserSubscriptionRepository;
import com.example.wordlearning.service.MembershipService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipServiceImpl implements MembershipService {
    public static final String PLAN_FREE = "FREE";
    public static final String PLAN_PRO = "PRO";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";

    private static final List<String> FREE_FEATURES = List.of(
            "每日单词练习",
            "错题本与收藏词",
            "公共词库搜索",
            "最多 20 个自定义词"
    );
    private static final List<String> PRO_FEATURES = List.of(
            "会员学习报告",
            "会员智能复习中心",
            "文章生词解析",
            "每日学习清单",
            "会员目标计划",
            "会员模考冲刺",
            "7 天复习计划",
            "薄弱词优先级诊断",
            "会员听写训练",
            "智能复习队列",
            "表格批量导入",
            "不限量自定义词",
            "学习热力图与今日总结"
    );

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final ActivationRedemptionRepository activationRedemptionRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String adminSecret;

    public MembershipServiceImpl(UserSubscriptionRepository userSubscriptionRepository,
                                 ActivationCodeRepository activationCodeRepository,
                                 ActivationRedemptionRepository activationRedemptionRepository,
                                 @Value("${app.admin.secret}") String adminSecret) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.activationCodeRepository = activationCodeRepository;
        this.activationRedemptionRepository = activationRedemptionRepository;
        this.adminSecret = adminSecret;
    }

    @Override
    @Transactional
    public MembershipResponse getMembership(Long userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .map(this::toMembership)
                .orElseGet(this::freeMembership);
    }

    @Override
    public List<MembershipPlanResponse> listPlans() {
        return List.of(
                new MembershipPlanResponse("PRO_30", "会员月卡", new BigDecimal("19.90"), 30, PRO_FEATURES),
                new MembershipPlanResponse("PRO_365", "会员年卡", new BigDecimal("99.00"), 365, PRO_FEATURES)
        );
    }

    @Override
    @Transactional
    public MembershipResponse redeem(Long userId, String code) {
        String normalizedCode = normalizeCode(code);
        ActivationCodeDO activationCode = activationCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Activation code does not exist"));
        validateCodeForRedeem(activationCode, userId);

        LocalDateTime now = LocalDateTime.now();
        UserSubscriptionDO subscription = userSubscriptionRepository.findByUserId(userId)
                .orElseGet(() -> newSubscription(userId, now));
        LocalDateTime baseTime = subscription.getExpireTime() != null && subscription.getExpireTime().isAfter(now)
                ? subscription.getExpireTime()
                : now;
        subscription.setPlanType(activationCode.getPlanType());
        subscription.setStatus(STATUS_ACTIVE);
        subscription.setStartTime(subscription.getStartTime() == null ? now : subscription.getStartTime());
        subscription.setExpireTime(baseTime.plusDays(activationCode.getDurationDays()));
        subscription.setSource("activation:" + normalizedCode);

        activationCode.setUsedCount(activationCode.getUsedCount() + 1);
        ActivationRedemptionDO redemption = new ActivationRedemptionDO();
        redemption.setCodeId(activationCode.getId());
        redemption.setCode(normalizedCode);
        redemption.setUserId(userId);
        redemption.setPlanType(activationCode.getPlanType());
        redemption.setDurationDays(activationCode.getDurationDays());

        activationCodeRepository.save(activationCode);
        activationRedemptionRepository.save(redemption);
        return toMembership(userSubscriptionRepository.save(subscription));
    }

    @Override
    public boolean isPro(Long userId) {
        MembershipResponse membership = getMembership(userId);
        return membership.pro();
    }

    @Override
    public void requirePro(Long userId, String featureName) {
        if (!isPro(userId)) {
            throw new PremiumRequiredException(featureName + "需要开通会员");
        }
    }

    @Override
    @Transactional
    public ActivationCodeResponse createActivationCode(String adminSecret, ActivationCodeRequest request) {
        requireAdmin(adminSecret);
        ActivationCodeRequest safeRequest = request == null
                ? new ActivationCodeRequest(null, null, null, null, null)
                : request;
        int durationDays = safeRequest.durationDays() == null ? 30 : Math.max(1, safeRequest.durationDays());
        int maxUses = safeRequest.maxUses() == null ? 1 : Math.max(1, safeRequest.maxUses());
        BigDecimal priceYuan = safeRequest.priceYuan() == null
                ? defaultPrice(durationDays)
                : safeRequest.priceYuan().max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
        String code = safeRequest.code() == null || safeRequest.code().isBlank()
                ? generateCode()
                : normalizeCode(safeRequest.code());
        if (activationCodeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Activation code already exists");
        }
        ActivationCodeDO activationCode = new ActivationCodeDO();
        activationCode.setCode(code);
        activationCode.setPlanType(PLAN_PRO);
        activationCode.setDurationDays(durationDays);
        activationCode.setMaxUses(maxUses);
        activationCode.setPriceYuan(priceYuan);
        activationCode.setUsedCount(0);
        activationCode.setStatus(STATUS_ACTIVE);
        activationCode.setNote(clean(safeRequest.note()));
        return toResponse(activationCodeRepository.save(activationCode));
    }

    @Override
    public List<ActivationCodeResponse> listActivationCodes(String adminSecret) {
        requireAdmin(adminSecret);
        return activationCodeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MonetizationSummaryResponse getMonetizationSummary(String adminSecret) {
        requireAdmin(adminSecret);
        List<ActivationCodeDO> codes = activationCodeRepository.findAll();
        int totalCapacity = codes.stream().mapToInt(item -> safeInt(item.getMaxUses())).sum();
        int redeemedUses = codes.stream().mapToInt(item -> safeInt(item.getUsedCount())).sum();
        BigDecimal estimatedRevenue = codes.stream()
                .map(item -> safePrice(item).multiply(BigDecimal.valueOf(safeInt(item.getUsedCount()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        long activeSubscriptions = userSubscriptionRepository.countByPlanTypeAndStatusAndExpireTimeAfter(
                PLAN_PRO,
                STATUS_ACTIVE,
                LocalDateTime.now()
        );
        return new MonetizationSummaryResponse(
                codes.size(),
                activeSubscriptions,
                totalCapacity,
                redeemedUses,
                Math.max(0, totalCapacity - redeemedUses),
                estimatedRevenue
        );
    }

    private void validateCodeForRedeem(ActivationCodeDO activationCode, Long userId) {
        if (!STATUS_ACTIVE.equals(activationCode.getStatus())) {
            throw new IllegalArgumentException("Activation code is not active");
        }
        if (activationCode.getExpiresAt() != null && activationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Activation code has expired");
        }
        if (activationCode.getUsedCount() >= activationCode.getMaxUses()) {
            throw new IllegalArgumentException("Activation code has been fully used");
        }
        if (activationRedemptionRepository.existsByCodeIdAndUserId(activationCode.getId(), userId)) {
            throw new IllegalArgumentException("You have already used this activation code");
        }
    }

    private UserSubscriptionDO newSubscription(Long userId, LocalDateTime now) {
        UserSubscriptionDO subscription = new UserSubscriptionDO();
        subscription.setUserId(userId);
        subscription.setStartTime(now);
        return subscription;
    }

    private MembershipResponse toMembership(UserSubscriptionDO subscription) {
        LocalDateTime now = LocalDateTime.now();
        boolean active = STATUS_ACTIVE.equals(subscription.getStatus())
                && PLAN_PRO.equals(subscription.getPlanType())
                && subscription.getExpireTime() != null
                && subscription.getExpireTime().isAfter(now);
        if (!active) {
            if (STATUS_ACTIVE.equals(subscription.getStatus())) {
                subscription.setStatus(STATUS_EXPIRED);
                userSubscriptionRepository.save(subscription);
            }
            return freeMembership();
        }
        long daysLeft = Math.max(0, ChronoUnit.DAYS.between(now.toLocalDate(), subscription.getExpireTime().toLocalDate()));
        return new MembershipResponse(PLAN_PRO, STATUS_ACTIVE, true, subscription.getExpireTime(), daysLeft,
                concat(FREE_FEATURES, PRO_FEATURES), List.of());
    }

    private MembershipResponse freeMembership() {
        return new MembershipResponse(PLAN_FREE, STATUS_ACTIVE, false, null, 0, FREE_FEATURES, PRO_FEATURES);
    }

    private List<String> concat(List<String> first, List<String> second) {
        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isBlank()) {
            throw new IllegalArgumentException("Activation code is required");
        }
        return code.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String generateCode() {
        byte[] bytes = new byte[6];
        String code;
        do {
            secureRandom.nextBytes(bytes);
            code = "PRO-" + HexFormat.of().formatHex(bytes).toUpperCase();
        } while (activationCodeRepository.existsByCode(code));
        return code;
    }

    private String clean(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private BigDecimal defaultPrice(int durationDays) {
        return durationDays >= 365 ? new BigDecimal("99.00") : new BigDecimal("19.90");
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safePrice(ActivationCodeDO activationCode) {
        return activationCode.getPriceYuan() == null ? BigDecimal.ZERO : activationCode.getPriceYuan();
    }

    private void requireAdmin(String secret) {
        if (secret == null || !secret.equals(adminSecret)) {
            throw new IllegalArgumentException("Invalid admin secret");
        }
    }

    private ActivationCodeResponse toResponse(ActivationCodeDO activationCode) {
        int remainingUses = Math.max(0, safeInt(activationCode.getMaxUses()) - safeInt(activationCode.getUsedCount()));
        BigDecimal priceYuan = safePrice(activationCode).setScale(2, java.math.RoundingMode.HALF_UP);
        return new ActivationCodeResponse(
                activationCode.getCode(),
                activationCode.getPlanType(),
                activationCode.getDurationDays(),
                activationCode.getMaxUses(),
                priceYuan,
                activationCode.getUsedCount(),
                remainingUses,
                priceYuan.multiply(BigDecimal.valueOf(safeInt(activationCode.getUsedCount()))),
                activationCode.getStatus(),
                activationCode.getNote(),
                activationCode.getCreatedTime()
        );
    }
}
