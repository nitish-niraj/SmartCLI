package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.commands.OsCommand;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.smart.AutoCompleter;

import java.util.Scanner;

public class ConsoleTerminal {
    private final FileSystem fs;
    private final SessionManager session;
    private final HistoryDatabase db;

    public ConsoleTerminal(FileSystem fs, SessionManager session, HistoryDatabase db) {
        this.fs = fs;
        this.session = session;
        this.db = db;
    }

    public void start() {
        System.out.println("Type 'help' to see all commands. Type 'exit' to quit.");

        Scanner scanner = new Scanner(System.in);
        OsCommand.setScanner(scanner);
        CommandParser parser = new CommandParser();
        AutoCompleter autoCompleter = new AutoCompleter(parser.getRegistry());

        while (true) {
            System.out.print("smartcli> ");

            String rawInput = scanner.nextLine();
            if (rawInput.endsWith("\t")) {
                String partial = rawInput.stripTrailing();
                String completed = autoCompleter.complete(partial);
                System.out.print("\r" + "smartcli> " + completed);
                continue;
            }

            if (rawInput == null || rawInput.isBlank()) {
                continue;
            }

            Command command = parser.parse(rawInput);
            String[] args = parser.getArgs(rawInput);

            if (command == null) {
                continue;
            }

            if (command instanceof ExitCommand) {
                command.execute(args, fs);
                break;
            }

            try {
                command.execute(args, fs);
            } catch (Exception e) {
                ErrorHandler.executionError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
        }

        System.out.println("Goodbye. Session ended.");
    }
}
