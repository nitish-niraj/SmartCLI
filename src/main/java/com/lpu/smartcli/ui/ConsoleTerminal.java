package com.lpu.smartcli.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.TerminalBuilder;

import com.lpu.smartcli.commands.DeleteCommand;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.smart.AutoCompleter;
import com.lpu.smartcli.smart.FuzzySearcher;
import com.lpu.smartcli.utils.AppLogger;

public class ConsoleTerminal {
    private final FileSystem fs;
    private final SessionManager session;
    private final HistoryDatabase db;
    private final ConfigManager config;
    private final FuzzySearcher fuzzySearcher = new FuzzySearcher();

    public ConsoleTerminal(FileSystem fs, SessionManager session, HistoryDatabase db) {
        this(fs, session, db, new ConfigManager());
    }

    public ConsoleTerminal(FileSystem fs, SessionManager session, HistoryDatabase db, ConfigManager config) {
        this.fs = fs;
        this.session = session;
        this.db = db;
        this.config = config;
    }

    public void start() {
        var logger = AppLogger.getLogger(ConsoleTerminal.class);
        logger.info("Type 'help' to see all commands. Type 'exit' to quit.");
        try {
            startJLine();
        } catch (Throwable e) {
            logger.warn("JLine terminal failed, falling back to scanner input", e);
            startScannerFallback();
        }
    }

    private void startJLine() throws IOException {
        AppLogger.getLogger(ConsoleTerminal.class).info("Tip: Use 'rsearch' (or Ctrl+R) to reverse-search history; Tab suggests commands, files, and fuzzy history matches.");
        CommandParser parser = new CommandParser(session, db, config, fs);
        AutoCompleter autoCompleter = new AutoCompleter(parser.getRegistry(), fs.getWorkingDirectory());
        Scanner scanner = new Scanner(System.in);
        OsCommand.setScanner(scanner);
        DeleteCommand.setScanner(scanner);

        org.jline.terminal.Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        DefaultHistory lineHistory = new DefaultHistory();
        List<String> recent = db.getRecentHistory(Math.max(1, config.getInt("historyLimit", 500)));
        for (int i = recent.size() - 1; i >= 0; i--) {
            lineHistory.add(recent.get(i));
        }

        LineReader reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .history(lineHistory)
            .completer((lineReader, parsedLine, candidates) -> {
                    autoCompleter.setWorkingDirectory(fs.getWorkingDirectory());
                    String word = parsedLine.word();
                    List<String> suggestions = parsedLine.wordIndex() == 0
                            ? mergeSuggestions(
                                    autoCompleter.suggestCommand(word),
                                    fuzzySearcher.search(word, db.getRecentHistory(400), 20))
                            : autoCompleter.suggestFile(word);
                    for (String suggestion : dedupePreserveOrder(suggestions)) {
                        candidates.add(new Candidate(suggestion));
                    }
                })
                .build();

        // Wire Ctrl+R (ASCII 0x12) to invoke interactive reverse-search
        reader.getWidgets().put("rsearch", () -> {
            try {
                new com.lpu.smartcli.commands.RSearchCommand(session, db, fs, parser).execute(null, fs);
            } catch (Exception ex) {
                AppLogger.getLogger(ConsoleTerminal.class).error("Error running rsearch widget", ex);
            }
            return true;
        });

        try {
            reader.getKeyMaps().get(LineReader.MAIN).bind(new Reference("rsearch"), "\u0012");
        } catch (Exception e) {
            AppLogger.getLogger(ConsoleTerminal.class).debug("Unable to bind Ctrl+R widget: {}", e.getMessage());
        }

        while (true) {
            String rawInput;
            try {
                rawInput = reader.readLine("smartcli> ");
            } catch (UserInterruptException e) {
                System.out.println();
                continue;
            } catch (EndOfFileException e) {
                break;
            }

            handleInput(rawInput, parser);
        }

        System.out.println("Goodbye. Session ended.");
    }

    private static List<String> mergeSuggestions(List<String> commandMatches, List<String> historyMatches) {
        List<String> merged = new ArrayList<>(commandMatches);
        merged.addAll(historyMatches);
        return merged;
    }

    private static List<String> dedupePreserveOrder(List<String> suggestions) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String suggestion : suggestions) {
            if (suggestion != null && !suggestion.isBlank()) {
                seen.add(suggestion);
            }
        }

        return new ArrayList<>(seen);
    }

    private void startScannerFallback() {
        Scanner scanner = new Scanner(System.in);
        OsCommand.setScanner(scanner);
        DeleteCommand.setScanner(scanner);
        CommandParser parser = new CommandParser(session, db, config, fs);
        AutoCompleter autoCompleter = new AutoCompleter(parser.getRegistry(), fs.getWorkingDirectory());

        while (true) {
            System.out.print("smartcli> ");
            String rawInput = scanner.nextLine();
            if (rawInput.endsWith("\t")) {
                autoCompleter.setWorkingDirectory(fs.getWorkingDirectory());
                String completed = autoCompleter.complete(rawInput.stripTrailing());
                System.out.print("\r" + "smartcli> " + completed);
                continue;
            }

            handleInput(rawInput, parser);
        }
    }

    public void handleInput(String rawInput, CommandParser parser) {
        if (rawInput == null || rawInput.isBlank()) {
            return;
        }

        session.recordCommand(rawInput);
        Command command = parser.parse(rawInput);
        String[] args = parser.getArgs(rawInput);

        if (command == null) {
            return;
        }

        try {
            command.execute(args, fs);
        } catch (Exception e) {
            ErrorHandler.executionError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }
}
