package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.FavoriteWordDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteWordRepository extends JpaRepository<FavoriteWordDO, Long> {
    List<FavoriteWordDO> findByUserIdOrderByCreatedTimeDesc(Long userId);

    Optional<FavoriteWordDO> findByUserIdAndWordIdAndWordType(Long userId, Long wordId, String wordType);

    void deleteByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}

