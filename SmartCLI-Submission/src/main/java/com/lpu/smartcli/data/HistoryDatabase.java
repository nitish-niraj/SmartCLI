package com.lpu.smartcli.data;

import java.util.ArrayList;
import java.util.List;

public class HistoryDatabase {
    private final List<Entry> entries = new ArrayList<>();

    public HistoryDatabase() {
    }

    HistoryDatabase(String customDbPath) {
    }

    public void addEntry(String commandText, String sessionId) {
        validateRequired(commandText, "Command text cannot be null or empty");
        validateRequired(sessionId, "Session ID cannot be null or empty");
        entries.add(new Entry(commandText, sessionId));
    }

    public List<String> getRecentHistory(int limit) {
        List<String> recent = new ArrayList<>();
        if (limit <= 0) {
            return recent;
        }

        for (int i = entries.size() - 1; i >= 0 && recent.size() < limit; i--) {
            recent.add(entries.get(i).commandText);
        }

        return recent;
    }

    public List<String> searchHistory(String query) {
        List<String> matches = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return matches;
        }

        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry entry = entries.get(i);
            if (entry.commandText.contains(query)) {
                matches.add(entry.commandText);
            }
        }

        return matches;
    }

    public void clearHistory() {
        entries.clear();
    }

    public int getTotalCount() {
        return entries.size();
    }

    public List<String> getSessionHistory(String sessionId) {
        List<String> sessionHistory = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank()) {
            return sessionHistory;
        }

        for (Entry entry : entries) {
            if (entry.sessionId.equals(sessionId)) {
                sessionHistory.add(entry.commandText);
            }
        }

        return sessionHistory;
    }

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static class Entry {
        private final String commandText;
        private final String sessionId;

        private Entry(String commandText, String sessionId) {
            this.commandText = commandText;
            this.sessionId = sessionId;
        }
    }
}
