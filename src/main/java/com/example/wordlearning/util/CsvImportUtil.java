package com.example.wordlearning.util;

import com.example.wordlearning.model.UserWordRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public final class CsvImportUtil {
    private CsvImportUtil() {
    }

    public static List<UserWordRequest> readUserWords(MultipartFile file) throws IOException {
        List<UserWordRequest> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            Map<String, Integer> headerMap = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isBlank()) {
                    continue;
                }
                List<String> columns = parseCsvLine(removeBom(line));
                if (headerMap == null && looksLikeHeader(columns)) {
                    headerMap = buildHeaderMap(columns);
                    continue;
                }
                UserWordRequest request = headerMap == null ? fromPlainColumns(columns) : fromHeaderColumns(columns, headerMap);
                if (request != null) {
                    result.add(request);
                }
            }
        }
        return result;
    }

    private static UserWordRequest fromHeaderColumns(List<String> columns, Map<String, Integer> headerMap) {
        UserWordRequest request = new UserWordRequest();
        request.setEnglish(getByHeader(columns, headerMap, "english", "word", "单词", "英文", "英语"));
        request.setChinese(getByHeader(columns, headerMap, "chinese", "meaning", "translation", "释义", "中文", "中文释义"));
        request.setPhonetic(getByHeader(columns, headerMap, "phonetic", "音标"));
        request.setExampleSentence(getByHeader(columns, headerMap, "examplesentence", "example", "sentence", "例句"));
        request.setCategory(getByHeader(columns, headerMap, "category", "分类", "词库"));
        request.setDifficulty(getByHeader(columns, headerMap, "difficulty", "难度"));
        return request;
    }

    private static UserWordRequest fromPlainColumns(List<String> columns) {
        if (columns.size() < 2) {
            return null;
        }
        int start = isSerialNumber(getColumn(columns, 0)) && columns.size() >= 3 ? 1 : 0;
        UserWordRequest request = new UserWordRequest();
        request.setEnglish(getColumn(columns, start));

        String second = getColumn(columns, start + 1);
        String third = getColumn(columns, start + 2);
        if (isLikelyPhonetic(second) && containsChinese(third)) {
            request.setPhonetic(second);
            request.setChinese(third);
            request.setExampleSentence(getColumn(columns, start + 3));
            request.setCategory(getColumn(columns, start + 4));
            request.setDifficulty(getColumn(columns, start + 5));
        } else {
            request.setChinese(second);
            request.setPhonetic(third);
            request.setExampleSentence(getColumn(columns, start + 3));
            request.setCategory(getColumn(columns, start + 4));
            request.setDifficulty(getColumn(columns, start + 5));
        }
        return request;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                columns.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString().trim());
        return columns;
    }

    private static boolean looksLikeHeader(List<String> columns) {
        String joined = String.join(",", columns).toLowerCase();
        return joined.contains("english") || joined.contains("chinese") || joined.contains("word")
                || joined.contains("meaning") || joined.contains("单词") || joined.contains("释义")
                || joined.contains("英文") || joined.contains("中文");
    }

    private static Map<String, Integer> buildHeaderMap(List<String> columns) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            headerMap.put(normalizeHeader(columns.get(i)), i);
        }
        return headerMap;
    }

    private static String getByHeader(List<String> columns, Map<String, Integer> headerMap, String... names) {
        for (String name : names) {
            Integer index = headerMap.get(normalizeHeader(name));
            if (index != null) {
                return getColumn(columns, index);
            }
        }
        return null;
    }

    private static String getColumn(List<String> columns, int index) {
        if (columns.size() <= index || index < 0) {
            return null;
        }
        String value = columns.get(index).trim();
        return value.isBlank() ? null : value;
    }

    private static String normalizeHeader(String value) {
        return removeBom(value).trim().toLowerCase().replace("_", "").replace(" ", "");
    }

    private static String removeBom(String value) {
        return value == null ? null : value.replace("\uFEFF", "");
    }

    private static boolean isSerialNumber(String value) {
        return value != null && value.matches("\\d+");
    }

    private static boolean containsChinese(String value) {
        return value != null && value.codePoints().anyMatch(code -> Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN);
    }

    private static boolean isLikelyPhonetic(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("[") || value.contains("/") || value.matches(".*[ːəɪʊɔæɑɒθðʃʒŋ].*");
    }
}
