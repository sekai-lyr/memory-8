package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.WrongWordDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongWordRepository extends JpaRepository<WrongWordDO, Long> {
    List<WrongWordDO> findByUserIdOrderByLastWrongTimeDesc(Long userId);

    Optional<WrongWordDO> findByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);

    void deleteByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}

