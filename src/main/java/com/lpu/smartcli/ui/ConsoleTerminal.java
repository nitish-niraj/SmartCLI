package com.lpu.smartcli.ui;

import com.lpu.smartcli.commands.DeleteCommand;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.smart.AutoCompleter;
import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ConsoleTerminal {
    private final FileSystem fs;
    private final SessionManager session;
    private final HistoryDatabase db;
    private final ConfigManager config;

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
        System.out.println("Type 'help' to see all commands. Type 'exit' to quit.");
        try {
            startJLine();
        } catch (Throwable e) {
            startScannerFallback();
        }
    }

    private void startJLine() throws IOException {
        CommandParser parser = new CommandParser(session, db, config);
        AutoCompleter autoCompleter = new AutoCompleter(parser.getRegistry(), fs.getWorkingDirectory());
        Scanner scanner = new Scanner(System.in);
        OsCommand.setScanner(scanner);
        DeleteCommand.setScanner(scanner);

        org.jline.terminal.Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer((lineReader, parsedLine, candidates) -> {
                    autoCompleter.setWorkingDirectory(fs.getWorkingDirectory());
                    List<String> suggestions = parsedLine.wordIndex() == 0
                            ? autoCompleter.suggestCommand(parsedLine.word())
                            : autoCompleter.suggestFile(parsedLine.word());
                    for (String suggestion : suggestions) {
                        candidates.add(new Candidate(suggestion));
                    }
                })
                .build();

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

    private void startScannerFallback() {
        Scanner scanner = new Scanner(System.in);
        OsCommand.setScanner(scanner);
        DeleteCommand.setScanner(scanner);
        CommandParser parser = new CommandParser(session, db, config);
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
