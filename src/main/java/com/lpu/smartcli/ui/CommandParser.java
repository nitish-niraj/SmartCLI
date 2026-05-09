package com.lpu.smartcli.ui;

import com.lpu.smartcli.commands.CreateCommand;
import com.lpu.smartcli.commands.DeleteCommand;
import com.lpu.smartcli.commands.ListCommand;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.commands.ReadCommand;
import com.lpu.smartcli.commands.WriteCommand;
import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.ai.NvidiaAIClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandParser {
    public static boolean AI_ENABLED = true;
    private static final Set<String> NATURAL_LANGUAGE_INDICATORS = new HashSet<>(List.of(
            "a", "the", "with", "called", "named", "my", "all", "me", "show", "make", "remove", "what"
    ));
    private static final Set<String> COMMON_OS_COMMANDS = new HashSet<>(List.of(
            "ls", "dir", "cd", "pwd", "echo", "type", "cat", "more", "copy", "xcopy",
            "move", "del", "rm", "mkdir", "rmdir", "cls", "clear", "tree", "find",
            "findstr", "grep", "where", "which", "whoami", "hostname", "date", "time",
            "ipconfig", "ifconfig", "ping", "tracert", "netstat", "curl", "wget",
            "git", "java", "javac", "mvn", "mvnw", "gradle", "npm", "node", "python",
            "python3", "pip", "powershell", "cmd", "sh", "bash"
    ));

    private Map<String, Command> registry;
    private NvidiaAIClient aiClient = new NvidiaAIClient();
    private String lastRawInput;
    private String lastInterpretedInput;

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
        lastRawInput = rawInput;
        lastInterpretedInput = null;

        List<String> tokens = tokenize(rawInput);
        if (tokens.isEmpty()) {
            return null;
        }

        boolean naturalLanguage = looksLikeNaturalLanguage(rawInput);
        if (AI_ENABLED && naturalLanguage && !isOsCommandAttempt(tokens)) {
            Command aiCommand = parseWithAi(rawInput);
            if (aiCommand != null) {
                return aiCommand;
            }

            if (containsWord(rawInput, "write")) {
                System.out.println("[AI] For writing use: write filename your content here");
                return null;
            }

            return new OsCommand(tokens.toArray(new String[0]));
        }

        Command found = registry.get(tokens.get(0));
        if (found == null) {
            if (AI_ENABLED && !isOsCommandAttempt(tokens)) {
                Command aiCommand = parseWithAi(rawInput);
                if (aiCommand != null) {
                    return aiCommand;
                }

                if (containsWord(rawInput, "write")) {
                    System.out.println("[AI] For writing use: write filename your content here");
                    return null;
                }
            }

            return new OsCommand(tokens.toArray(new String[0]));
        }

        return found;
    }

    public Map<String, Command> getRegistry() {
        return registry;
    }

    public String[] getArgs(String rawInput) {
        String inputToTokenize = rawInput;
        if (lastInterpretedInput != null && rawInput != null && rawInput.equals(lastRawInput)) {
            inputToTokenize = lastInterpretedInput;
        }

        List<String> tokens = tokenize(inputToTokenize);
        if (tokens.size() <= 1) {
            return new String[0];
        }

        return tokens.subList(1, tokens.size()).toArray(new String[0]);
    }

    private Command getInterpretedCommand(String interpreted) {
        List<String> interpretedTokens = tokenize(interpreted);
        if (interpretedTokens.isEmpty()) {
            return null;
        }

        if ("write".equals(interpretedTokens.get(0)) && interpretedTokens.size() < 3) {
            System.out.println("[AI] Use: write filename your content here");
            return null;
        }

        return registry.get(interpretedTokens.get(0));
    }

    private Command parseWithAi(String rawInput) {
        String interpreted = aiClient.interpret(rawInput);
        Command interpretedCommand = getInterpretedCommand(interpreted);
        if (interpretedCommand == null) {
            return null;
        }

        lastInterpretedInput = interpreted.trim();
        System.out.println("[AI] Interpreted as: " + lastInterpretedInput);
        return interpretedCommand;
    }

    private boolean looksLikeNaturalLanguage(String input) {
        List<String> tokens = tokenize(input);
        if (tokens.size() <= 3) {
            return false;
        }

        for (String token : tokens) {
            if (NATURAL_LANGUAGE_INDICATORS.contains(token.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private boolean containsWord(String input, String word) {
        for (String token : tokenize(input)) {
            if (token.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }

    private boolean isOsCommandAttempt(List<String> tokens) {
        if (tokens.isEmpty()) {
            return false;
        }

        String commandName = tokens.get(0).toLowerCase();
        return COMMON_OS_COMMANDS.contains(commandName)
                || commandName.contains("\\")
                || commandName.contains("/")
                || commandName.endsWith(".exe")
                || commandName.endsWith(".bat")
                || commandName.endsWith(".cmd")
                || commandName.endsWith(".ps1");
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
