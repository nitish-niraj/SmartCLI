package com.lpu.smartcli.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class SessionManager {
    private final String sessionId;
    private String currentDirectory;
    private final LocalDateTime startTime;
    private final HistoryDatabase history;

    public SessionManager() {
        this(new HistoryDatabase());
    }

    public SessionManager(HistoryDatabase history) {
        if (history == null) {
            throw new IllegalArgumentException("HistoryDatabase cannot be null");
        }

        this.sessionId = UUID.randomUUID().toString();
        this.currentDirectory = System.getProperty("user.dir");
        this.startTime = LocalDateTime.now();
        this.history = history;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public String getCurrentDir() {
        return getCurrentDirectory();
    }

    public void setCurrentDirectory(Path path) {
        if (path != null) {
            currentDirectory = path.toAbsolutePath().normalize().toString();
        }
    }

    public boolean changeDirectory(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(currentDirectory, path);
            }

            if (file.exists() && file.isDirectory()) {
                currentDirectory = file.getCanonicalPath();
                return true;
            }

            System.err.println("ERROR: Directory not found: " + path);
            return false;
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            return false;
        }
    }

    public void recordCommand(String commandText) {
        if (commandText == null || commandText.isBlank()) {
            return;
        }

        history.addEntry(commandText, sessionId);
    }

    public List<String> getSessionHistory() {
        return history.getSessionHistory(sessionId);
    }

    public String getStartTime() {
        return startTime.toString();
    }

    public long getSessionDurationSeconds() {
        return ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    public String getSummary() {
        return "Session ID : " + sessionId
                + System.lineSeparator() + "Started    : " + startTime
                + System.lineSeparator() + "Directory  : " + currentDirectory
                + System.lineSeparator() + "Commands   : " + getSessionHistory().size();
    }
}
