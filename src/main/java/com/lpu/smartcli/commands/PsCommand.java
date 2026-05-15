package com.lpu.smartcli.commands;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.integration.ProcessManager;

public class PsCommand implements Command {
    private static final int DISPLAY_CAP = 200;
    private final ProcessManager processManager = new ProcessManager();

    @Override
    public void execute(String[] args, FileSystem fs) {
        String filter = null;
        if (args != null && args.length > 0) {
            filter = String.join(" ", args).trim();
            if (filter.isEmpty()) {
                filter = null;
            }
        }

        List<ProcessManager.ProcessInfo> processes = filter == null
            ? processManager.getAllProcesses()
            : processManager.searchByName(filter);

        int total = processes.size();
        if (total > DISPLAY_CAP) {
            processes = processes.subList(0, DISPLAY_CAP);
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        System.out.printf(Locale.US, "%-10s %-12s %-20s %s%n", "PID", "START", "USER", "COMMAND");
        System.out.println("-".repeat(90));

        for (ProcessManager.ProcessInfo info : processes) {
            String started = info.startTimeEpochSecond > 0
                    ? timeFormatter.format(Instant.ofEpochSecond(info.startTimeEpochSecond))
                    : "?";
            System.out.printf(Locale.US, "%-10d %-12s %-20s %s%n",
                    info.pid,
                    started,
                    truncate(info.user, 20),
                    truncate(info.command, 60));
        }

        System.out.println("-".repeat(90));
        System.out.println("Showing " + processes.size() + " of " + total + " matching processes"
                + (filter == null ? "" : " (filter: '" + filter + "')"));
        if (total > DISPLAY_CAP) {
            System.out.println("Narrow with: ps <name fragment>");
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }

        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    @Override
    public String getDescription() {
        return "ps — List processes (optional name filter)";
    }
}
