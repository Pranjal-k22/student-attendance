package com.attendance.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class FileUtils {
    public static void ensureFile(Path p) {
        try {
            if (Files.notExists(p.getParent())) Files.createDirectories(p.getParent());
            if (Files.notExists(p)) Files.createFile(p);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create file: " + p + " : " + e.getMessage(), e);
        }
    }

    public static List<String> readAllLines(Path p) throws IOException {
        if (Files.notExists(p)) return Collections.emptyList();
        return Files.readAllLines(p, StandardCharsets.UTF_8);
    }

    public static void writeLines(Path p, List<String> lines) throws IOException {
        Files.write(p, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void appendLine(Path p, String line) throws IOException {
        Files.write(p, Collections.singletonList(line), StandardCharsets.UTF_8,
                Files.exists(p) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    }
}
