package com.lpu.smartcli.ui;

import com.lpu.smartcli.commands.CreateCommand;
import com.lpu.smartcli.commands.DeleteCommand;
import com.lpu.smartcli.commands.ListCommand;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.commands.ReadCommand;
import com.lpu.smartcli.commands.WriteCommand;
import com.lpu.smartcli.core.Command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {
    private Map<String, Command> registry;

    public CommandParser() {
        registry = new LinkedHashMap<>();
        registry.put("create", new CreateCommand());
        registry.put("write", new WriteCommand());
        registry.put("read", new ReadCommand());
        registry.put("delete", new DeleteCommand());
        registry.put("list", new ListCommand());
        registry.put("help", new HelpCommand(registry));
        registry.put("exit", new ExitCommand());
    }

    public Command parse(String rawInput) {
        List<String> tokens = tokenize(rawInput);
        if (tokens.isEmpty()) {
            return null;
        }

        Command found = registry.get(tokens.get(0));
        if (found == null) {
            return new OsCommand(tokens.toArray(new String[0]));
        }

        return found;
    }

    public Map<String, Command> getRegistry() {
        return registry;
    }

    public String[] getArgs(String rawInput) {
        List<String> tokens = tokenize(rawInput);
        if (tokens.size() <= 1) {
            return new String[0];
        }

        return tokens.subList(1, tokens.size()).toArray(new String[0]);
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null) {
            return tokens;
        }

        if (input.isBlank()) {
            return tokens;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '"') {
                inQuotes = !inQuotes;
            } else if (currentChar == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(currentChar);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }
}
