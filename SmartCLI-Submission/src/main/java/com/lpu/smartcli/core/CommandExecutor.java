package com.lpu.smartcli.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    public static CommandResult execute(String commandString) {
        try {
            String[] shellPrefix = PlatformDetector.getShellPrefix();
            String[] command = new String[shellPrefix.length + 1];
            System.arraycopy(shellPrefix, 0, command, 0, shellPrefix.length);
            command[shellPrefix.length] = commandString;

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            String stdout;
            try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                stdout = readFully(stdoutReader);
            }

            String stderr;
            try (BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                stderr = readFully(stderrReader);
            }

            int exitCode = process.waitFor();
            return new CommandResult(stdout, stderr, exitCode);
        } catch (Exception exception) {
            return new CommandResult("", exception.getMessage(), -1);
        }
    }

    public static void main(String[] args) {
        CommandResult result = execute("echo SmartCLI Phase 3 Working");
        System.out.print(result.getStdout());
    }

    private static String readFully(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return String.join(System.lineSeparator(), lines);
    }
}
