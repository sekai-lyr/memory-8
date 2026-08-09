package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.MasteredWordDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasteredWordRepository extends JpaRepository<MasteredWordDO, Long> {
    List<MasteredWordDO> findByUserId(Long userId);

    Optional<MasteredWordDO> findByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);

    boolean existsByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);

    void deleteByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);
}
