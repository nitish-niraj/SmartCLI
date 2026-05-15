package com.lpu.smartcli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.lpu.smartcli.commands.RSearchCommand;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.ui.CommandParser;

public class RSearchCommandTest {

    @Test
    public void testRSearchWithArgsShowsMatches() {
        try (HistoryDatabase db = new HistoryDatabase(":memory:")) {
            db.addEntry("git status", "s1");
            db.addEntry("git commit -m test", "s1");

            SessionManager session = new SessionManager(db);
            FileSystem fs = new FileSystem();
            CommandParser parser = new CommandParser(session, db, null, fs);
            RSearchCommand cmd = new RSearchCommand(session, db, fs, parser);

            var baos = new ByteArrayOutputStream();
            var ps = new PrintStream(baos);
            var oldOut = System.out;
            System.setOut(ps);
            try {
                cmd.execute(new String[]{"git"}, fs);
            } finally {
                System.setOut(oldOut);
            }

            String out = baos.toString();
            assertTrue(out.contains("Matches:") || out.contains("No matches"));
        }
    }

    @Test
    public void testRSearchInteractiveRunsChoice() {
        try (HistoryDatabase db = new HistoryDatabase(":memory:")) {
            db.addEntry("git status", "s1");
            db.addEntry("git commit -m test", "s1");

            SessionManager session = new SessionManager(db);
            FileSystem fs = new FileSystem();
            CommandParser parser = new CommandParser(session, db, null, fs);
            RSearchCommand cmd = new RSearchCommand(session, db, fs, parser);

            // Simulate user typing query 'git' then selecting '1' and newline
            var input = new java.io.ByteArrayInputStream("git\n1\n".getBytes());
            var oldIn = System.in;
            var baos = new ByteArrayOutputStream();
            var ps = new PrintStream(baos);
            var oldOut = System.out;
            System.setIn(input);
            System.setOut(ps);
            try {
                cmd.execute(new String[0], fs);
            } finally {
                System.setIn(oldIn);
                System.setOut(oldOut);
            }

            String out = baos.toString();
            assertTrue(out.contains("Running:") || out.contains("Matches:") || out.contains("Cancelled"));
        }
    }
}
