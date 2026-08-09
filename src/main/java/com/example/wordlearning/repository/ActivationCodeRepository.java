package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.ActivationCodeDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationCodeRepository extends JpaRepository<ActivationCodeDO, Long> {
    Optional<ActivationCodeDO> findByCode(String code);

    boolean existsByCode(String code);
}
