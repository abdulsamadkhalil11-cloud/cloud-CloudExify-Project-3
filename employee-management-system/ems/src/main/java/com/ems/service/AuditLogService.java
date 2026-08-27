package com.ems.service;

import com.ems.exceptions.FileOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Plain-text "Recent Activity Log" bonus feature - every notable action gets one line. */
public class AuditLogService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String logPath;

    public AuditLogService(String logPath) {
        this.logPath = logPath;
    }

    public void log(String action) throws FileOperationException {
        try {
            Path path = Paths.get(logPath);
            Files.createDirectories(path.getParent());
            String line = "[" + LocalDateTime.now().format(STAMP) + "] " + action + System.lineSeparator();
            Files.write(path, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FileOperationException("Could not write to activity log", e);
        }
    }

    /** Same as log(), but swallows failures - used for routine actions where losing a log line isn't worth interrupting the user. */
    public void logQuiet(String action) {
        try {
            log(action);
        } catch (FileOperationException e) {
            System.out.println("  (activity log unavailable: " + e.getMessage() + ")");
        }
    }

    public List<String> getRecent(int count) throws FileOperationException {
        Path path = Paths.get(logPath);
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<String> allLines = Files.readAllLines(path);
            int from = Math.max(0, allLines.size() - count);
            return allLines.subList(from, allLines.size());
        } catch (IOException e) {
            throw new FileOperationException("Could not read activity log", e);
        }
    }
}
