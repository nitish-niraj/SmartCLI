package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.CommandExecutor;
import com.lpu.smartcli.core.CommandResult;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.utils.SafetyFilter;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OsCommand implements Command {
    private static Scanner sharedScanner;
    private static volatile CommandResult lastResult;

    private final String[] tokens;

    public OsCommand(String[] tokens) {
        this.tokens = tokens == null ? new String[0] : tokens.clone();
    }

    public static void setScanner(Scanner scanner) {
        sharedScanner = scanner;
    }

    public static CommandResult getLastResult() {
        return lastResult;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        String commandLine = String.join(" ", tokens).trim();
        if (commandLine.isEmpty()) {
            return;
        }

        Scanner scanner = sharedScanner == null ? new Scanner(System.in) : sharedScanner;
        SafetyFilter.FilterResult filterResult = SafetyFilter.check(commandLine, scanner);
        if (filterResult == SafetyFilter.FilterResult.DANGEROUS
                || filterResult == SafetyFilter.FilterResult.INTERACTIVE
                || filterResult == SafetyFilter.FilterResult.BLOCKED) {
            return;
        }

        Path workingDirectory = fs == null ? Path.of(System.getProperty("user.dir")) : fs.getWorkingDirectory();
        CompletableFuture<CommandResult> future = CommandExecutor.executeAsync(
                commandLine,
                workingDirectory,
                System.out::println,
                System.err::println
        );

        if (filterResult == SafetyFilter.FilterResult.LONG_RUNNING) {
            try {
                lastResult = future.get(10, TimeUnit.SECONDS);
                System.out.println("OS command exited with code: " + lastResult.getExitCode());
            } catch (Exception e) {
                future.cancel(true);
                System.out.println("[OS] Command timed out or requires interactive input — not supported.");
            }
            return;
        }

        future.thenAccept(result -> {
            lastResult = result;
            System.out.println("OS command exited with code: " + result.getExitCode());
        });
    }

    @Override
    public String getDescription() {
        return "Runs an operating system command";
    }
}
