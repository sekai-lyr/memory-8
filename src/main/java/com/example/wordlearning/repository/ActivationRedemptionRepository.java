package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.ActivationRedemptionDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationRedemptionRepository extends JpaRepository<ActivationRedemptionDO, Long> {
    boolean existsByCodeIdAndUserId(Long codeId, Long userId);
}
