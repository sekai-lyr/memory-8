package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.ActivationCodeDO;
import com.example.wordlearning.model.ActivationCodeRequest;
import com.example.wordlearning.model.ActivationCodeResponse;
import com.example.wordlearning.model.MembershipResponse;
import com.example.wordlearning.repository.ActivationCodeRepository;
import com.example.wordlearning.repository.ActivationRedemptionRepository;
import com.example.wordlearning.repository.UserSubscriptionRepository;
import com.example.wordlearning.service.impl.MembershipServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipServiceImplTest {
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private ActivationCodeRepository activationCodeRepository;

    @Mock
    private ActivationRedemptionRepository activationRedemptionRepository;

    private MembershipServiceImpl membershipService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipServiceImpl(
                userSubscriptionRepository,
                activationCodeRepository,
                activationRedemptionRepository,
                "admin-secret"
        );
    }

    @Test
    void missingSubscriptionReturnsFreeMembershipWithLockedProFeatures() {
        when(userSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MembershipResponse response = membershipService.getMembership(1L);

        assertThat(response.pro()).isFalse();
        assertThat(response.planType()).isEqualTo("FREE");
        assertThat(response.lockedFeatures()).contains("表格批量导入", "智能复习队列");
    }

    @Test
    void adminCanCreateActivationCode() {
        when(activationCodeRepository.existsByCode("PRO-TEST")).thenReturn(false);
        when(activationCodeRepository.save(any(ActivationCodeDO.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivationCodeResponse response = membershipService.createActivationCode(
                "admin-secret",
                new ActivationCodeRequest("pro-test", 30, 1, new java.math.BigDecimal("19.90"), "manual sale")
        );

        assertThat(response.code()).isEqualTo("PRO-TEST");
        assertThat(response.planType()).isEqualTo("PRO");
        assertThat(response.durationDays()).isEqualTo(30);
        assertThat(response.maxUses()).isEqualTo(1);
        assertThat(response.priceYuan()).isEqualByComparingTo("19.90");
    }
}
