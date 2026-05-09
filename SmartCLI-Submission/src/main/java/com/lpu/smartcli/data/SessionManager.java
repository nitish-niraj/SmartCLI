package com.lpu.smartcli.data;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    public SessionManager() {
    }

    public String getSessionId() {
        return "";
    }

    public String getCurrentDirectory() {
        return "";
    }

    public boolean changeDirectory(String path) {
        return false;
    }

    public void recordCommand(String commandText) {
    }

    public List<String> getSessionHistory() {
        return new ArrayList<>();
    }

    public String getStartTime() {
        return "";
    }

    public long getSessionDurationSeconds() {
        return 0;
    }

    public String getSummary() {
        return "";
    }
}
