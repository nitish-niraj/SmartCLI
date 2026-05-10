package com.lpu.smartcli.smart;

import com.lpu.smartcli.core.Command;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutoCompleter {
    private final List<String> knownCommands;
    private Path workingDirectory;

    public AutoCompleter(Map<String, Command> registry) {
        this(registry, Path.of(System.getProperty("user.dir")));
    }

    public AutoCompleter(Map<String, Command> registry, Path workingDirectory) {
        this.knownCommands = new ArrayList<>(registry.keySet());
        this.workingDirectory = workingDirectory == null ? Path.of(System.getProperty("user.dir")) : workingDirectory;
    }

    public void setWorkingDirectory(Path workingDirectory) {
        if (workingDirectory != null) {
            this.workingDirectory = workingDirectory;
        }
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

        try (var paths = Files.list(workingDirectory)) {
            paths.map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(safePartial))
                    .sorted()
                    .forEach(matches::add);
        } catch (Exception e) {
            return matches;
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
        if ("git".equalsIgnoreCase(tokens[0]) && tokens.length == 2) {
            List<String> gitMatches = suggestGitSubcommand(lastToken);
            if (gitMatches.size() == 1) {
                tokens[tokens.length - 1] = gitMatches.get(0);
                return String.join(" ", tokens);
            }

            if (gitMatches.size() > 1) {
                System.out.println(String.join("  ", gitMatches));
            }
            return input;
        }

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

    private List<String> suggestGitSubcommand(String partial) {
        String safePartial = partial == null ? "" : partial.toLowerCase();
        return Arrays.asList("commit", "push", "pull", "status", "log", "clone").stream()
                .filter(command -> command.startsWith(safePartial))
                .toList();
    }
}
