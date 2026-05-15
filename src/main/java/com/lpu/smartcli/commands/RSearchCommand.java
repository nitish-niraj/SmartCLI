package com.lpu.smartcli.commands;

import java.util.List;
import java.util.Scanner;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.smart.FuzzySearcher;
import com.lpu.smartcli.ui.CommandParser;
import com.lpu.smartcli.utils.AppLogger;

public class RSearchCommand implements Command {
    private final SessionManager session;
    private final HistoryDatabase history;
    private final FileSystem fs;
    private final CommandParser parser;
    private final FuzzySearcher fuzzy = new FuzzySearcher();

    public RSearchCommand(SessionManager session, HistoryDatabase history, FileSystem fs, CommandParser parser) {
        this.session = session;
        this.history = history;
        this.fs = fs;
        this.parser = parser;
    }

    @Override
    public void execute(String[] args, FileSystem ignored) {
        String query;
        if (args != null && args.length > 0) {
            query = String.join(" ", args).trim();
        } else {
            System.out.print("reverse-search> ");
            Scanner scanner = new Scanner(System.in);
            query = scanner.nextLine().strip();
        }

        if (query == null || query.isBlank()) {
            AppLogger.getLogger(RSearchCommand.class).info("No query provided.");
            return;
        }

        List<String> recent = history.getRecentHistory(1000);
        var results = fuzzy.searchWithScores(query, recent, 20);
        if (results.isEmpty()) {
            AppLogger.getLogger(RSearchCommand.class).info("No matches found for: {}", query);
            return;
        }

        AppLogger.getLogger(RSearchCommand.class).info("Matches:");
        for (int i = 0; i < results.size(); i++) {
            var r = results.get(i);
            AppLogger.getLogger(RSearchCommand.class).info("{} ) {}", i + 1, r.toString());
        }

        System.out.print("Select number to run (0 to cancel): ");
        Scanner selScanner = new Scanner(System.in);
        String selRaw = selScanner.nextLine();
        int sel = -1;
        try {
            sel = Integer.parseInt(selRaw.trim());
        } catch (Exception ignoredEx) {
        }

        if (sel <= 0 || sel > results.size()) {
            AppLogger.getLogger(RSearchCommand.class).info("Cancelled.");
            return;
        }

        String chosen = results.get(sel - 1).command;
        AppLogger.getLogger(RSearchCommand.class).info("Running: {}", chosen);
        session.recordCommand(chosen);
        var cmd = parser.parse(chosen);
        if (cmd == null) {
            // Fallback: run as OS command
            var os = new com.lpu.smartcli.commands.OsCommand(chosen.split(" "));
            os.execute(new String[0], fs);
            return;
        }

        var argsFor = parser.getArgs(chosen);
        try {
            cmd.execute(argsFor, fs);
        } catch (Exception e) {
            System.out.println("Error executing chosen command: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Interactive fuzzy reverse search through history (Ctrl+R style)";
    }
}
