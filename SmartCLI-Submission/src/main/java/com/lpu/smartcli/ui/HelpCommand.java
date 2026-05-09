package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HelpCommand implements Command {
    private Map<String, Command> registry;

    public HelpCommand(Map<String, Command> registry) {
        this.registry = registry;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.out.println("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557");
        System.out.println("\u2551           Smart CLI — Available Commands         \u2551");
        System.out.println("\u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563");

        for (Map.Entry<String, Command> entry : registry.entrySet()) {
            String name = entry.getKey();
            String description = getDisplayDescription(name, entry.getValue().getDescription());
            System.out.printf("\u2551  %-7s — %-38s\u2551%n", name, description);
        }

        System.out.println("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D");
    }

    @Override
    public String getDescription() {
        return "help — Show this help message";
    }

    private String getDisplayDescription(String name, String description) {
        int dashIndex = description.indexOf('—');
        if (dashIndex >= 0 && dashIndex + 1 < description.length()) {
            return description.substring(dashIndex + 1).trim();
        }

        return description;
    }
}
