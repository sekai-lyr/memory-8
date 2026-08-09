package com.example.wordlearning.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProGoalRequest(
        @NotBlank(message = "目标名称不能为空")
        String targetName,
        @NotNull(message = "目标日期不能为空")
        @FutureOrPresent(message = "目标日期不能早于今天")
        LocalDate targetDate,
        @NotNull(message = "每日学习时间不能为空")
        @Min(value = 5, message = "每日学习时间至少 5 分钟")
        @Max(value = 240, message = "每日学习时间不能超过 240 分钟")
        Integer dailyMinutes,
        @NotNull(message = "目标词量不能为空")
        @Min(value = 20, message = "目标词量至少 20 个")
        @Max(value = 20000, message = "目标词量不能超过 20000 个")
        Integer targetWordCount
) {
}
