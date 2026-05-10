package com.lpu.smartcli.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FileSystem {
    private HashMap<String, String> files = new HashMap<>();
    private Path workingDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();

    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Working directory cannot be null");
        }

        workingDirectory = path.toAbsolutePath().normalize();
    }

    public void createFile(String name) {
        validateFileName(name);
        Path target = resolvePath(name);

        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.exists(target)) {
                System.out.println("File '" + name + "' already exists.");
                return;
            }

            Files.createFile(target);
            files.put(name, "");
            System.out.println("File '" + name + "' created at " + target);
        } catch (IOException e) {
            System.out.println("Error creating file '" + name + "': " + e.getMessage());
        }
    }

    public String readFile(String name) {
        validateFileName(name);
        Path target = resolvePath(name);

        if (!Files.exists(target)) {
            throwFileNotFound(name);
        }

        try {
            return Files.readString(target);
        } catch (NoSuchFileException e) {
            throwFileNotFound(name);
            return null;
        } catch (IOException e) {
            System.out.println("Error reading file '" + name + "': " + e.getMessage());
            return null;
        }
    }

    public void writeFile(String name, String content) {
        validateFileName(name);
        Path target = resolvePath(name);

        if (!Files.exists(target)) {
            throwFileNotFound(name);
        }

        try {
            String safeContent = content == null ? "" : content;
            Files.writeString(target, safeContent);
            files.put(name, safeContent);
            System.out.println("Written to '" + target + "'.");
        } catch (IOException e) {
            System.out.println("Error writing file '" + name + "': " + e.getMessage());
        }
    }

    public void deleteFile(String name) {
        validateFileName(name);
        Path target = resolvePath(name);

        if (!Files.exists(target)) {
            throwFileNotFound(name);
        }

        try {
            Files.delete(target);
            files.remove(name);
            System.out.println("File '" + name + "' deleted.");
        } catch (IOException e) {
            System.out.println("Error deleting file '" + name + "': " + e.getMessage());
        }
    }

    public boolean fileExists(String name) {
        if (name == null) {
            return false;
        }

        return Files.exists(resolvePath(name));
    }

    public List<String> listFiles() {
        try (var paths = Files.list(workingDirectory)) {
            return new ArrayList<>(paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .toList());
        } catch (IOException e) {
            System.out.println("Error listing files in '" + workingDirectory + "': " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getFileCount() {
        return files.size();
    }

    public void clearAll() {
        for (String name : new ArrayList<>(files.keySet())) {
            try {
                Files.deleteIfExists(resolvePath(name));
            } catch (IOException e) {
                System.out.println("Error deleting file '" + name + "': " + e.getMessage());
            }
        }

        files.clear();
    }

    public Path resolvePath(String name) {
        validateFileName(name);
        return workingDirectory.resolve(name).toAbsolutePath().normalize();
    }

    private void validateFileName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
    }

    private static void throwFileNotFound(String name) {
        FileSystem.<RuntimeException>sneakyThrow(new FileNotFoundException("File not found: " + name));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
