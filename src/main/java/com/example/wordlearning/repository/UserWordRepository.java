package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.UserWordDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWordRepository extends JpaRepository<UserWordDO, Long> {
    List<UserWordDO> findByUserIdOrderByIdDesc(Long userId);

    List<UserWordDO> findByUserIdAndEnglishIgnoreCaseContainingOrUserIdAndChineseContaining(
            Long englishUserId, String english, Long chineseUserId, String chinese);

    boolean existsByUserIdAndEnglishIgnoreCase(Long userId, String english);

    long countByUserId(Long userId);

    Optional<UserWordDO> findByIdAndUserId(Long id, Long userId);
}

