package com.lpu.smartcli.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lpu.smartcli.ai.NvidiaAIClient;
import com.lpu.smartcli.commands.AliasCommand;
import com.lpu.smartcli.commands.CdCommand;
import com.lpu.smartcli.commands.CreateCommand;
import com.lpu.smartcli.commands.DeleteCommand;
import com.lpu.smartcli.commands.GitDiffCommand;
import com.lpu.smartcli.commands.GitLogCommand;
import com.lpu.smartcli.commands.GitStatusCommand;
import com.lpu.smartcli.commands.KillCommand;
import com.lpu.smartcli.commands.ListCommand;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.commands.PsCommand;
import com.lpu.smartcli.commands.PwdCommand;
import com.lpu.smartcli.commands.ReadCommand;
import com.lpu.smartcli.commands.ThemeCommand;
import com.lpu.smartcli.commands.WriteCommand;
import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.plugins.PluginManager;
import com.lpu.smartcli.smart.AutoCorrect;
import com.lpu.smartcli.storage.AliasStore;
import com.lpu.smartcli.utils.AppLogger;

public class CommandParser {
    public static boolean AI_ENABLED = true;
    private static final Set<String> NATURAL_LANGUAGE_INDICATORS = new HashSet<>(List.of(
            "a", "the", "with", "called", "named", "my", "all", "me", "show", "make", "remove", "what",
            "one", "new", "file", "program", "in"
    ));
    private static final Set<String> COMMON_OS_COMMANDS = new HashSet<>(List.of(
            "ls", "dir", "echo", "type", "cat", "more", "copy", "xcopy",
            "move", "del", "rm", "mkdir", "rmdir", "cls", "clear", "tree", "find",
            "findstr", "grep", "where", "which", "whoami", "hostname", "date", "time",
            "ipconfig", "ifconfig", "ping", "tracert", "netstat", "curl", "wget",
            "git", "java", "javac", "mvn", "mvnw", "gradle", "npm", "node", "python",
            "python3", "pip", "powershell", "cmd", "sh", "bash"
    ));

    private final Map<String, Command> registry;
    private final AliasStore aliasStore;
    private NvidiaAIClient aiClient;
    private boolean aiAvailable = true;
    private String lastRawInput;
    private String lastInterpretedInput;

    public CommandParser() {
        this(null, new HistoryDatabase(":memory:"), new ConfigManager(), null);
    }

    public CommandParser(ConfigManager configManager) {
        this(null, new HistoryDatabase(":memory:"), configManager, null);
    }

    public CommandParser(SessionManager session, HistoryDatabase history, ConfigManager configManager) {
        this(session, history, configManager, null);
    }

    public CommandParser(SessionManager session, HistoryDatabase history, ConfigManager configManager, FileSystem persistenceFileSystem) {
        registry = new LinkedHashMap<>();
        aliasStore = new AliasStore(configManager);
        ThemeManager themeManager = new ThemeManager(configManager);

        registry.put("create", new CreateCommand());
        registry.put("write", new WriteCommand());
        registry.put("read", new ReadCommand());
        registry.put("delete", new DeleteCommand());
        registry.put("list", new ListCommand());
        registry.put("cd", new CdCommand(session));
        registry.put("pwd", new PwdCommand());
        registry.put("alias", new AliasCommand(aliasStore));
        registry.put("theme", new ThemeCommand(themeManager));
        registry.put("gitstatus", new GitStatusCommand());
        registry.put("gitlog", new GitLogCommand());
        registry.put("gitdiff", new GitDiffCommand());
        registry.put("ps", new PsCommand());
        registry.put("kill", new KillCommand());
        registry.put("rsearch", new com.lpu.smartcli.commands.RSearchCommand(session, history, persistenceFileSystem, this));

        Path pluginDir = Path.of(System.getProperty("user.home"), ".smartcli", "plugins");
        new PluginManager(pluginDir).registerPlugins(registry);

        registry.put("help", new HelpCommand(registry));
        registry.put("exit", new ExitCommand(history, configManager, persistenceFileSystem, session));

        try {
            aiClient = new NvidiaAIClient();
        } catch (Exception e) {
            aiAvailable = false;
        }
    }

    public Command parse(String rawInput) {
        lastRawInput = rawInput;
        lastInterpretedInput = null;

        List<String> tokens = tokenize(rawInput);
        if (tokens.isEmpty()) {
            return null;
        }

        String routedInput = expandAlias(rawInput, tokens);
        if (!routedInput.equals(rawInput)) {
            lastInterpretedInput = routedInput;
            tokens = tokenize(routedInput);
            AppLogger.getLogger(CommandParser.class).info("[alias] {} -> {}", rawInput, routedInput);
        }

        Command found = registry.get(tokens.get(0));
        boolean naturalLanguage = looksLikeNaturalLanguage(routedInput);
        if (found != null) {
            if (AI_ENABLED && naturalLanguage && shouldUseAiForKnownCommand(tokens) && !isOsCommandAttempt(tokens)) {
                Command aiCommand = tryParseWithAi(routedInput, tokens);
                if (aiCommand != null) {
                    return aiCommand;
                }
            }

            return found;
        }

        Optional<String> suggestion = suggestCommand(tokens.get(0));
        if (suggestion.isPresent() && !tokens.get(0).equalsIgnoreCase(suggestion.get())) {
            AppLogger.getLogger(CommandParser.class).info("Unknown command. Did you mean: {}?", suggestion.get());
            return null;
        }

        if (AI_ENABLED && !isOsCommandAttempt(tokens)) {
            Command aiCommand = tryParseWithAi(routedInput, tokens);
            if (aiCommand != null) {
                return aiCommand;
            }
        }

        return new OsCommand(tokens.toArray(new String[0]));
    }

    public Map<String, Command> getRegistry() {
        return registry;
    }

    public Optional<String> suggestCommandName(String commandName) {
        List<String> known = new ArrayList<>(registry.keySet());
        known.addAll(COMMON_OS_COMMANDS);
        return AutoCorrect.suggest(commandName, known);
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

    private Command tryParseWithAi(String rawInput, List<String> tokens) {
        if (!aiAvailable) {
            String localInterpretation = normalizeInterpretedCommand(interpretLocally(rawInput));
            Command localCommand = getInterpretedCommand(localInterpretation);
            if (localCommand != null) {
                lastInterpretedInput = localInterpretation;
                AppLogger.getLogger(CommandParser.class).info("[AI] Interpreted as: {}", lastInterpretedInput);
                return localCommand;
            }

                if (looksLikeNaturalLanguage(rawInput)) {
                    printAiUnavailable();
                }
            return null;
        }

        Command aiCommand = parseWithAi(rawInput);
        if (aiCommand != null) {
            return aiCommand;
        }

        if (containsWord(rawInput, "write")) {
            AppLogger.getLogger(CommandParser.class).info("[AI] For writing use: write filename your content here");
            return null;
        }

        return isOsCommandAttempt(tokens) ? new OsCommand(tokens.toArray(new String[0])) : null;
    }

    private Command getInterpretedCommand(String interpreted) {
        List<String> interpretedTokens = tokenize(interpreted);
        if (interpretedTokens.isEmpty()) {
            return null;
        }

        if ("write".equals(interpretedTokens.get(0)) && interpretedTokens.size() < 3) {
            AppLogger.getLogger(CommandParser.class).info("[AI] Use: write filename your content here");
            return null;
        }

        return registry.get(interpretedTokens.get(0));
    }

    private Command parseWithAi(String rawInput) {
        String interpreted = aiClient.interpret(rawInput);
        if (interpreted == null || interpreted.isBlank()) {
            interpreted = interpretLocally(rawInput);
        }

        interpreted = normalizeInterpretedCommand(interpreted);
        Command interpretedCommand = getInterpretedCommand(interpreted);
        if (interpretedCommand == null) {
            return null;
        }

        lastInterpretedInput = interpreted.trim();
        AppLogger.getLogger(CommandParser.class).info("[AI] Interpreted as: {}", lastInterpretedInput);
        return interpretedCommand;
    }

    private String normalizeInterpretedCommand(String interpreted) {
        if (interpreted == null) {
            return null;
        }

        String trimmed = interpreted.trim();
        if (!trimmed.startsWith("{")) {
            return firstLine(trimmed);
        }

        try {
            JsonObject object = JsonParser.parseString(trimmed).getAsJsonObject();
            String command = object.get("command").getAsString();
            List<String> args = new ArrayList<>();
            JsonElement argsElement = object.get("args");
            if (argsElement != null && argsElement.isJsonArray()) {
                JsonArray array = argsElement.getAsJsonArray();
                for (JsonElement element : array) {
                    args.add(element.getAsString());
                }
            }

            return (command + " " + String.join(" ", args)).trim();
        } catch (Exception e) {
            AppLogger.getLogger(CommandParser.class).debug("Failed to parse AI JSON: {}", e.getMessage());
            return firstLine(trimmed);
        }
    }

    private String interpretLocally(String rawInput) {
        List<String> tokens = tokenize(rawInput);
        int filenameIndex = findLastFilenameIndex(tokens);
        if (filenameIndex < 0) {
            return null;
        }

        if (containsWord(rawInput, "create") || containsWord(rawInput, "make")) {
            return "{\"command\":\"create\",\"args\":[\"" + tokens.get(filenameIndex) + "\"]}";
        }

        if (containsWord(rawInput, "write")) {
            int writeIndex = findWordIndex(tokens, "write");
            if (writeIndex < 0 || writeIndex >= filenameIndex) {
                return null;
            }

            List<String> contentTokens = new ArrayList<>(tokens.subList(writeIndex + 1, filenameIndex));
            while (!contentTokens.isEmpty() && isWritePreposition(contentTokens.get(contentTokens.size() - 1))) {
                contentTokens.remove(contentTokens.size() - 1);
            }

            if (contentTokens.isEmpty()) {
                return null;
            }

            return "write " + tokens.get(filenameIndex) + " " + String.join(" ", contentTokens);
        }

        return null;
    }

    private String expandAlias(String rawInput, List<String> tokens) {
        return aliasStore.resolve(tokens.get(0))
                .map(alias -> {
                    List<String> rest = tokens.size() > 1 ? tokens.subList(1, tokens.size()) : List.of();
                    return (alias + " " + String.join(" ", rest)).trim();
                })
                .orElse(rawInput);
    }

    private Optional<String> suggestCommand(String commandName) {
        return suggestCommandName(commandName);
    }

    private String firstLine(String text) {
        int newlineIndex = text.indexOf('\n');
        return newlineIndex >= 0 ? text.substring(0, newlineIndex).trim() : text;
    }

    private int findLastFilenameIndex(List<String> tokens) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (looksLikeFilename(tokens.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private int findWordIndex(List<String> tokens, String word) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equalsIgnoreCase(word)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isWritePreposition(String token) {
        return "in".equalsIgnoreCase(token)
                || "to".equalsIgnoreCase(token)
                || "into".equalsIgnoreCase(token);
    }

    private void printAiUnavailable() {
        AppLogger.getLogger(CommandParser.class).warn("[AI] AI layer unavailable. Please create config.properties with your NVIDIA API key.");
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

    private boolean shouldUseAiForKnownCommand(List<String> tokens) {
        String commandName = tokens.get(0).toLowerCase();
        if ("write".equals(commandName)) {
            return tokens.size() < 2 || !looksLikeFilename(tokens.get(1));
        }

        if ("create".equals(commandName) || "read".equals(commandName) || "delete".equals(commandName)) {
            return tokens.size() != 2 || !looksLikeFilename(tokens.get(1));
        }

        return false;
    }

    private boolean looksLikeFilename(String token) {
        if (token == null) {
            return false;
        }

        return token.matches("(?i).+\\.[a-z0-9]+");
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null || input.isBlank()) {
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
