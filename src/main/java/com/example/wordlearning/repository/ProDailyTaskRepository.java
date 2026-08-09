package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.ProDailyTaskDO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProDailyTaskRepository extends JpaRepository<ProDailyTaskDO, Long> {
    List<ProDailyTaskDO> findByUserIdAndTaskDateOrderBySortOrderAsc(Long userId, LocalDate taskDate);

    Optional<ProDailyTaskDO> findByIdAndUserId(Long id, Long userId);

    Optional<ProDailyTaskDO> findByUserIdAndTaskDateAndTaskKey(Long userId, LocalDate taskDate, String taskKey);

    @Query("select distinct t.taskDate from ProDailyTaskDO t where t.userId = ?1 and t.completed = true order by t.taskDate desc")
    List<LocalDate> findCompletedDates(Long userId);
}
