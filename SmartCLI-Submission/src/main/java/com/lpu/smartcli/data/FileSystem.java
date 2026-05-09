package com.lpu.smartcli.data;

import com.lpu.smartcli.core.ErrorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileSystem {
    private HashMap<String, String> files = new HashMap<>();

    public void createFile(String name) {
        if (name == null || name.isBlank()) {
            System.out.println("Error: Filename cannot be empty.");
            return;
        }

        if (fileExists(name)) {
            ErrorHandler.alreadyExists(name);
        } else {
            files.put(name, "");
            System.out.println("File '" + name + "' created.");
        }
    }

    public String readFile(String name) {
        if (!fileExists(name)) {
            ErrorHandler.fileNotFound(name);
            return null;
        }

        return files.get(name);
    }

    public void writeFile(String name, String content) {
        if (name == null || name.isBlank()) {
            System.out.println("Error: Filename cannot be empty.");
            return;
        }

        if (!fileExists(name)) {
            ErrorHandler.fileNotFound(name);
        } else {
            files.put(name, content);
            System.out.println("Written to '" + name + "'.");
        }
    }

    public void deleteFile(String name) {
        if (!fileExists(name)) {
            ErrorHandler.fileNotFound(name);
        } else {
            files.remove(name);
            System.out.println("File '" + name + "' deleted.");
        }
    }

    public boolean fileExists(String name) {
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
}
