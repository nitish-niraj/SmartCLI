package com.lpu.smartcli.ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;

public class Terminal {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        FileSystem fs = new FileSystem();
        HistoryDatabase db = new HistoryDatabase();
        SessionManager session = new SessionManager(db);
        ConfigManager config = new ConfigManager();

        printBanner();

        String mode = parseMode(args);
        if ("gui".equals(mode)) {
            launchGui(fs, session, db, config);
        } else {
            launchConsole(fs, session, db, config);
        }
    }

    private static String parseMode(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--mode".equals(args[i])) {
                return args[i + 1].toLowerCase();
            }
        }

        return "console";
    }

    private static void printBanner() {
        System.out.println("+--------------------------------------+");
        System.out.println("|   Smart Command Line System v1.0     |");
        System.out.println("|   LPU | CAP477 | Section D2526       |");
        System.out.println("|   Supervisor: Dr. Prince Arora       |");
        System.out.println("+--------------------------------------+");
    }

    private static void launchConsole(FileSystem fs, SessionManager session, HistoryDatabase db, ConfigManager config) {
        ConsoleTerminal console = new ConsoleTerminal(fs, session, db, config);
        console.start();
    }

    private static void launchGui(FileSystem fs, SessionManager session, HistoryDatabase db, ConfigManager config) {
        TerminalPane.launchGui(fs, session, db, config);
    }
}
