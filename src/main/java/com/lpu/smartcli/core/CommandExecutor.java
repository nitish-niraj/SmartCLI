package com.lpu.smartcli.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommandExecutor {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "smartcli-command-executor");
        thread.setDaemon(true);
        return thread;
    });

    private CommandExecutor() {
    }

    public static CommandResult execute(String commandString) {
        return executeAsync(commandString).join();
    }

    public static CompletableFuture<CommandResult> executeAsync(String commandString) {
        return executeAsync(commandString, Path.of(System.getProperty("user.dir")), line -> {
        }, line -> {
        });
    }

    public static CompletableFuture<CommandResult> executeAsync(
            String commandString,
            Path workingDirectory,
            Consumer<String> stdoutConsumer,
            Consumer<String> stderrConsumer
    ) {
        return CompletableFuture.supplyAsync(() -> run(commandString, workingDirectory, stdoutConsumer, stderrConsumer), EXECUTOR);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    private static CommandResult run(
            String commandString,
            Path workingDirectory,
            Consumer<String> stdoutConsumer,
            Consumer<String> stderrConsumer
    ) {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try {
            String[] shellPrefix = PlatformDetector.getShellPrefix();
            String[] command = new String[shellPrefix.length + 1];
            System.arraycopy(shellPrefix, 0, command, 0, shellPrefix.length);
            command[shellPrefix.length] = commandString;

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false);
            if (workingDirectory != null) {
                processBuilder.directory(workingDirectory.toFile());
            }

            Process process = processBuilder.start();

            CompletableFuture<Void> stdoutFuture = stream(process.getInputStream(), stdout, stdoutConsumer);
            CompletableFuture<Void> stderrFuture = stream(process.getErrorStream(), stderr, stderrConsumer);

            int exitCode = process.waitFor();
            CompletableFuture.allOf(stdoutFuture, stderrFuture).join();

            return new CommandResult(stdout.toString(), stderr.toString(), exitCode);
        } catch (Exception exception) {
            String message = exception.getMessage() == null ? "Unknown execution error" : exception.getMessage();
            stderrConsumer.accept(message);
            return new CommandResult(stdout.toString(), message, -1);
        }
    }

    private static CompletableFuture<Void> stream(InputStream inputStream, StringBuilder collector, Consumer<String> consumer) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    synchronized (collector) {
                        if (collector.length() > 0) {
                            collector.append(System.lineSeparator());
                        }
                        collector.append(line);
                    }
                    consumer.accept(line);
                }
            } catch (IOException e) {
                consumer.accept("Error reading process output: " + e.getMessage());
            }
        }, EXECUTOR);
    }
}
