package com.example.wordlearning.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnswerJudgeUtil {
    private AnswerJudgeUtil() {
    }

    public static boolean isCorrect(String userAnswer, String correctAnswer) {
        Set<String> userParts = splitAndNormalize(userAnswer);
        Set<String> correctParts = splitAndNormalize(correctAnswer);
        if (userParts.isEmpty() || correctParts.isEmpty()) {
            return false;
        }
        for (String part : userParts) {
            if (correctParts.contains(part)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> splitAndNormalize(String text) {
        if (text == null) {
            return Set.of();
        }
        return Arrays.stream(text.trim()
                        .replace("。", "")
                        .replace(".", "")
                        .replace("：", "")
                        .replace(":", "")
                        .split("[；;、，,/]"))
                .map(AnswerJudgeUtil::removePartOfSpeech)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
    }

    private static String removePartOfSpeech(String value) {
        return value.replaceAll("(?i)^(n|v|adj|adv|prep|conj|pron|num|art|vi|vt)\\s*", "")
                .replaceAll("^(名词|动词|形容词|副词)\\s*", "")
                .trim();
    }
}
