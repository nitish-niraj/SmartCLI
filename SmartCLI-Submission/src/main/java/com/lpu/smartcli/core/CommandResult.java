package com.lpu.smartcli.core;
import java.time.LocalDateTime;

public class CommandResult {
    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final LocalDateTime timestamp;

    public CommandResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public int getExitCode() { return exitCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return exitCode == 0; }
}
