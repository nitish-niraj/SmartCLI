package com.lpu.smartcli.core;

/**
 * CommandResult encapsulates the result of command execution.
 * Contains stdout, stderr, exit code, and execution timestamp.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class CommandResult {

    private String stdout;
    private String stderr;
    private int exitCode;
    private long timestamp;

    /**
     * Constructs a CommandResult with all parameters.
     *
     * @param stdout   the standard output from command execution
     * @param stderr   the standard error from command execution
     * @param exitCode the exit code of the command
     */
    public CommandResult(String stdout, String stderr, int exitCode, long timestamp) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.timestamp = timestamp;
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
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets stdout output.
     *
     * @param stdout new stdout value
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * Sets stderr output.
     *
     * @param stderr new stderr value
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * Sets process exit code.
     *
     * @param exitCode new exit code
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Sets result timestamp.
     *
     * @param timestamp new timestamp value
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
