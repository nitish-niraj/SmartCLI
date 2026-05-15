package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.integration.ProcessManager;

import java.util.Optional;

public class KillCommand implements Command {
    private final ProcessManager processManager = new ProcessManager();

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: kill <pid> [--force]");
            return;
        }

        boolean force = false;
        String pidToken = args[0];
        if (args.length > 1 && "--force".equalsIgnoreCase(args[args.length - 1])) {
            force = true;
        }

        long pid;
        try {
            pid = Long.parseLong(pidToken.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid PID: " + pidToken);
            return;
        }

        long self = ProcessHandle.current().pid();
        if (pid == self) {
            System.out.println("Refusing to terminate the current SmartCLI process.");
            return;
        }

        Optional<ProcessManager.ProcessInfo> info = processManager.findByPid(pid);
        if (info.isEmpty()) {
            System.out.println("No such process: " + pid);
            return;
        }

        boolean destroyed = force ? destroyForcibly(pid) : processManager.killProcess(pid);
        if (!destroyed) {
            System.out.println("Could not signal process " + pid + ". Try: kill " + pid + " --force");
            return;
        }

        System.out.println((force ? "Forcibly destroyed " : "Sent destroy to ") + "PID " + pid
                + " (" + info.get().command + ")");
    }

    private boolean destroyForcibly(long pid) {
        return ProcessHandle.of(pid)
                .map(handle -> {
                    handle.destroyForcibly();
                    return true;
                })
                .orElse(false);
    }

    @Override
    public String getDescription() {
        return "kill — Terminate a process by PID (optional --force)";
    }
}
