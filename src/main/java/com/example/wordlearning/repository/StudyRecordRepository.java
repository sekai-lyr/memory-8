package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.StudyRecordDO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRecordRepository extends JpaRepository<StudyRecordDO, Long> {
    List<StudyRecordDO> findByUserIdOrderByCreatedTimeDesc(Long userId);
}

