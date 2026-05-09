package com.lpu.smartcli.smart;

import com.lpu.smartcli.core.Command;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoCompleter {
    private List<String> knownCommands;

    public AutoCompleter(Map<String, Command> registry) {
        knownCommands = new ArrayList<>(registry.keySet());
    }

    public List<String> suggestCommand(String partial) {
        List<String> matches = new ArrayList<>();
        String safePartial = partial == null ? "" : partial.toLowerCase();

        for (String command : knownCommands) {
            if (command.toLowerCase().startsWith(safePartial)) {
                matches.add(command);
            }
        }

        return matches;
    }

    public List<String> suggestFile(String partial) {
        List<String> matches = new ArrayList<>();
        String safePartial = partial == null ? "" : partial;
        File currentDirectory = new File(System.getProperty("user.dir"));
        File[] files = currentDirectory.listFiles();

        if (files == null) {
            return matches;
        }

        for (File file : files) {
            if (file.getName().startsWith(safePartial)) {
                matches.add(file.getName());
            }
        }

        return matches;
    }

    public String complete(String rawInput) {
        String input = rawInput == null ? "" : rawInput;
        String[] tokens = input.split(" ", -1);

        if (tokens.length <= 1) {
            List<String> matches = suggestCommand(tokens.length == 0 ? "" : tokens[0]);
            if (matches.size() == 1) {
                return matches.get(0) + " ";
            }

            if (matches.size() > 1) {
                System.out.println(String.join("  ", matches));
            }

            return input;
        }

        String lastToken = tokens[tokens.length - 1];
        List<String> matches = suggestFile(lastToken);
        if (matches.size() == 1) {
            tokens[tokens.length - 1] = matches.get(0);
            return String.join(" ", tokens);
        }

        if (matches.size() > 1) {
            System.out.println(String.join("  ", matches));
        }

        return input;
    }
}
