package com.lpu.smartcli.core;

import java.time.LocalDateTime;

/**
 * CommandResult encapsulates the result of command execution.
 * Contains stdout, stderr, exit code, and execution timestamp.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class CommandResult {

    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final LocalDateTime timestamp;

    /**
     * Constructs a CommandResult with all parameters.
     *
     * @param stdout   the standard output from command execution
     * @param stderr   the standard error from command execution
     * @param exitCode the exit code of the command
     */
    public CommandResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout != null ? stdout : "";
        this.stderr = stderr != null ? stderr : "";
        this.exitCode = exitCode;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the standard output.
     *
     * @return the stdout string
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Gets the standard error.
     *
     * @return the stderr string
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Gets the exit code.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets the execution timestamp.
     *
     * @return the timestamp of execution
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the command execution was successful.
     *
     * @return true if exit code is 0, false otherwise
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", exitCode=" + exitCode +
                ", timestamp=" + timestamp +
                '}';
    }
}
