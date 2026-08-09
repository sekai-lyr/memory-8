package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.ProStudyGoalDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProStudyGoalRepository extends JpaRepository<ProStudyGoalDO, Long> {
    Optional<ProStudyGoalDO> findByUserId(Long userId);
}
