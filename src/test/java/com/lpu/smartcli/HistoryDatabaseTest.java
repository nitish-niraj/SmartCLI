package com.lpu.smartcli;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.lpu.smartcli.data.HistoryDatabase;

public class HistoryDatabaseTest {

    @Test
    public void testAddAndRetrieveRecent() {
        try (HistoryDatabase db = new HistoryDatabase(":memory:")) {
            db.addEntry("git status", "s1");
            db.addEntry("ls -la", "s1");
            List<String> recent = db.getRecentHistory(5);
            assertTrue(recent.contains("git status"));
            assertTrue(recent.contains("ls -la"));
        }
    }

    @Test
    public void testSearchHistory() {
        try (HistoryDatabase db = new HistoryDatabase(":memory:")) {
            db.addEntry("git status", "s1");
            db.addEntry("git commit -m test", "s1");
            List<String> matches = db.searchHistory("commit");
            assertFalse(matches.isEmpty());
            assertTrue(matches.get(0).contains("commit") || matches.get(0).contains("git"));
        }
    }
}
