package com.lpu.smartcli.data;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileSystem {
    private HashMap<String, String> files = new HashMap<>();

    public void createFile(String name) {
        validateFileName(name);

        if (fileExists(name)) {
            throw new IllegalStateException("File already exists: " + name);
        }

        files.put(name, "");
        System.out.println("File '" + name + "' created.");
    }

    public String readFile(String name) {
        validateFileName(name);

        if (!fileExists(name)) {
            throwFileNotFound(name);
        }

        return files.get(name);
    }

    public void writeFile(String name, String content) {
        validateFileName(name);

        if (!fileExists(name)) {
            throwFileNotFound(name);
        }

        files.put(name, content == null ? "" : content);
        System.out.println("Written to '" + name + "'.");
    }

    public void deleteFile(String name) {
        validateFileName(name);

        if (!fileExists(name)) {
            throwFileNotFound(name);
        }

        files.remove(name);
        System.out.println("File '" + name + "' deleted.");
    }

    public boolean fileExists(String name) {
        if (name == null) {
            return false;
        }

        return files.containsKey(name);
    }

    public List<String> listFiles() {
        return new ArrayList<>(files.keySet());
    }

    public int getFileCount() {
        return files.size();
    }

    public void clearAll() {
        files.clear();
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
