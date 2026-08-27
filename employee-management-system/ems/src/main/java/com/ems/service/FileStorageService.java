package com.ems.service;

import com.ems.exceptions.FileOperationException;
import com.ems.model.SystemData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * All disk I/O goes through here: object serialization for the main
 * data file, plus timestamped backup/restore copies. Every failure
 * is wrapped in a {@link FileOperationException} so callers never
 * have to deal with raw {@link IOException}.
 */
public class FileStorageService {

    public void save(SystemData data, String path) throws FileOperationException {
        try {
            Path filePath = Paths.get(path);
            Files.createDirectories(filePath.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                out.writeObject(data);
            }
        } catch (IOException e) {
            throw new FileOperationException("Failed to save data to " + path, e);
        }
    }

    public SystemData load(String path) throws FileOperationException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileOperationException("Data file not found: " + path);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (SystemData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new FileOperationException("Failed to load data from " + path, e);
        }
    }

    public boolean exists(String path) {
        return new File(path).exists();
    }

    public String backup(String sourcePath, String backupDir) throws FileOperationException {
        try {
            Files.createDirectories(Paths.get(backupDir));
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = backupDir + "/employees_" + stamp + ".bak";
            Files.copy(Paths.get(sourcePath), Paths.get(backupPath));
            return backupPath;
        } catch (IOException e) {
            throw new FileOperationException("Backup failed", e);
        }
    }

    public void restore(String backupFilePath, String targetPath) throws FileOperationException {
        try {
            Files.copy(Paths.get(backupFilePath), Paths.get(targetPath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileOperationException("Restore failed", e);
        }
    }

    public java.util.List<String> listBackups(String backupDir) {
        File dir = new File(backupDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".bak"));
        if (files == null) {
            return java.util.List.of();
        }
        java.util.List<String> names = new java.util.ArrayList<>();
        for (File f : files) {
            names.add(f.getPath());
        }
        java.util.Collections.sort(names);
        return names;
    }
}
