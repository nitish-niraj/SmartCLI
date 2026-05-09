package com.lpu.smartcli.integration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProcessManager {

    public static class ProcessInfo {
        public final long pid;
        public final String command;
        public final String user;
        public final long startTimeEpochSecond;

        public ProcessInfo(long pid, String command, String user, long startTimeEpochSecond) {
            this.pid = pid;
            this.command = command;
            this.user = user;
            this.startTimeEpochSecond = startTimeEpochSecond;
        }

        @Override
        public String toString() {
            return String.format("PID: %6d | %-40s | %s", pid, command, user);
        }
    }

    public List<ProcessInfo> getAllProcesses() {
        return ProcessHandle.allProcesses()
                .map(this::toProcessInfo)
                .filter(info -> !("unknown".equals(info.command) && "unknown".equals(info.user)))
                .sorted(Comparator.comparingLong(info -> info.pid))
                .toList();
    }

    public boolean killProcess(long pid) {
        try {
            Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
            if (processHandle.isEmpty()) {
                return false;
            }

            processHandle.get().destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<ProcessInfo> findByPid(long pid) {
        try {
            return ProcessHandle.of(pid).map(this::toProcessInfo);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<ProcessInfo> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }

        String lowerName = name.toLowerCase();

        return getAllProcesses().stream()
                .filter(info -> info.command.toLowerCase().contains(lowerName))
                .toList();
    }

    public int getProcessCount() {
        return getAllProcesses().size();
    }

    private ProcessInfo toProcessInfo(ProcessHandle processHandle) {
        ProcessHandle.Info info = processHandle.info();
        String command = extractCommandName(info.command().orElse("unknown"));
        String user = info.user().orElse("unknown");
        long startTimeEpochSecond = info.startInstant()
                .map(instant -> instant.getEpochSecond())
                .orElse(0L);

        return new ProcessInfo(processHandle.pid(), command, user, startTimeEpochSecond);
    }

    private String extractCommandName(String fullCommand) {
        if (fullCommand == null || fullCommand.isBlank() || "unknown".equals(fullCommand)) {
            return "unknown";
        }

        try {
            Path fileName = Paths.get(fullCommand).getFileName();
            if (fileName == null) {
                return "unknown";
            }

            return fileName.toString();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
