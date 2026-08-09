package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.UserSubscriptionDO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionDO, Long> {
    Optional<UserSubscriptionDO> findByUserId(Long userId);

    long countByPlanTypeAndStatusAndExpireTimeAfter(String planType, String status, LocalDateTime expireTime);
}
