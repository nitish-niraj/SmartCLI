package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class OsCommand implements Command {
    private final String[] tokens;

    public OsCommand(String[] tokens) {
        this.tokens = tokens == null ? new String[0] : tokens.clone();
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        String commandLine = String.join(" ", tokens).trim();
        if (commandLine.isEmpty()) {
            return;
        }

        ProcessBuilder processBuilder = createProcessBuilder(commandLine);
        try {
            Process process = processBuilder.start();

            Thread stdoutThread = streamOutput(process.getInputStream(), false);
            Thread stderrThread = streamOutput(process.getErrorStream(), true);

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            System.out.println("OS command exited with code: " + exitCode);
        } catch (IOException e) {
            System.out.println("Failed to run OS command: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("OS command interrupted.");
        }
    }

    @Override
    public String getDescription() {
        return "Runs an operating system command";
    }

    private ProcessBuilder createProcessBuilder(String commandLine) {
        if (isWindows()) {
            return new ProcessBuilder("cmd.exe", "/c", commandLine);
        }

        return new ProcessBuilder("sh", "-c", commandLine);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private Thread streamOutput(InputStream inputStream, boolean errorStream) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (errorStream) {
                        System.err.println(line);
                    } else {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading OS command output: " + e.getMessage());
            }
        });

        thread.start();
        return thread;
    }
}
