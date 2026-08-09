package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.LearningStatusDO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningStatusRepository extends JpaRepository<LearningStatusDO, Long> {
    Optional<LearningStatusDO> findByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);

    List<LearningStatusDO> findByUserIdOrderByUpdatedTimeDesc(Long userId);

    @Query("select s.status, count(s) from LearningStatusDO s where s.userId = ?1 group by s.status")
    List<Object[]> countByStatus(Long userId);

    long countByUserIdAndNextReviewTimeLessThanEqualAndStatusNot(Long userId, LocalDateTime now, String status);
}
