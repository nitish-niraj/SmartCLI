package com.lpu.smartcli.utils;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class SafetyFilter {
    public enum FilterResult {
        SAFE,
        DANGEROUS,
        INTERACTIVE,
        LONG_RUNNING,
        BLOCKED
    }

    private static final List<String> DANGEROUS_COMMANDS = List.of(
            "shutdown",
            "format",
            "rm -rf",
            "rmdir /s",
            "mkfs",
            "dd",
            "reg delete",
            "cipher /w",
            "del /f /s",
            ":(){ :|:& };:"
    );

    private static final List<String> SENSITIVE_COMMANDS = List.of(
            "ipconfig /release",
            "git reset --hard",
            "git clean",
            "git push",
            "net user",
            "del",
            "rm",
            "rmdir",
            "mv",
            "move",
            "copy",
            "xcopy",
            "robocopy",
            "taskkill",
            "kill",
            "pkill",
            "regedit",
            "reg",
            "netsh",
            "arp",
            "route",
            "attrib",
            "icacls",
            "chmod",
            "chown",
            "passwd"
    );

    private static final List<String> INTERACTIVE_COMMANDS = List.of(
            "time",
            "date",
            "pause",
            "more",
            "ftp",
            "telnet",
            "notepad",
            "calc",
            "explorer",
            "mspaint",
            "cmd",
            "bash"
    );

    private SafetyFilter() {
    }

    public static FilterResult check(String input, Scanner scanner) {
        String command = input == null ? "" : input.trim();
        String normalized = normalize(command);

        if (startsWithAny(normalized, DANGEROUS_COMMANDS)) {
            System.out.println("[BLOCKED] This command can cause irreversible system damage. SmartCLI has permanently blocked it.");
            return FilterResult.DANGEROUS;
        }

        if (startsWithAny(normalized, INTERACTIVE_COMMANDS)) {
            System.out.println("[BLOCKED] Interactive commands are not supported in SmartCLI.");
            return FilterResult.INTERACTIVE;
        }

        if (startsWithAny(normalized, SENSITIVE_COMMANDS)) {
            System.out.print("[PERMISSION] You are about to run: '" + command
                    + "'. This may modify or delete system/git data. Are you sure? (yes/no): ");
            String answer = scanner.nextLine();
            if ("yes".equalsIgnoreCase(answer.trim())) {
                return FilterResult.SAFE;
            }

            System.out.println("[CANCELLED] Command was not executed.");
            return FilterResult.BLOCKED;
        }

        if (isLongRunning(normalized)) {
            System.out.println("[WARNING] This command may run forever. A 10 second timeout has been applied.");
            return FilterResult.LONG_RUNNING;
        }

        return FilterResult.SAFE;
    }

    private static boolean startsWithAny(String command, List<String> patterns) {
        for (String pattern : patterns) {
            if (command.equals(pattern) || command.startsWith(pattern + " ")) {
                return true;
            }
        }

        return false;
    }

    private static boolean isLongRunning(String command) {
        return isPingWithoutCount(command)
                || command.equals("tail -f")
                || command.startsWith("tail -f ")
                || command.equals("watch")
                || command.startsWith("watch ")
                || command.equals("top")
                || command.startsWith("top ")
                || command.equals("htop")
                || command.startsWith("htop ")
                || isCurlWithoutMaxTime(command);
    }

    private static boolean isPingWithoutCount(String command) {
        if (!command.equals("ping") && !command.startsWith("ping ")) {
            return false;
        }

        return !containsToken(command, "-n") && !containsToken(command, "-c");
    }

    private static boolean isCurlWithoutMaxTime(String command) {
        if (!command.equals("curl") && !command.startsWith("curl ")) {
            return false;
        }

        return !containsToken(command, "-m");
    }

    private static boolean containsToken(String command, String token) {
        for (String part : command.split("\\s+")) {
            if (part.equals(token)) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String input) {
        return input.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
