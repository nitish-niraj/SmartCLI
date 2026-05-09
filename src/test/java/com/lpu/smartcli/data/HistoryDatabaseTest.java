package com.lpu.smartcli.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistoryDatabaseTest {
    private HistoryDatabase historyDatabase;

    @BeforeEach
    void setUp() {
        historyDatabase = new HistoryDatabase(":memory:");
    }

    @Test
    void addEntryIncreasesTotalCount() {
        historyDatabase.addEntry("git status", "session-1");

        assertEquals(1, historyDatabase.getTotalCount());
    }

    @Test
    void addEntryThrowsWhenCommandTextIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> historyDatabase.addEntry(null, "session-1")
        );
    }

    @Test
    void addEntryThrowsWhenCommandTextIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> historyDatabase.addEntry("   ", "session-1")
        );
    }

    @Test
    void addEntryThrowsWhenSessionIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> historyDatabase.addEntry("git status", null)
        );
    }

    @Test
    void getRecentHistoryReturnsMostRecentFirstUpToLimit() {
        historyDatabase.addEntry("git status", "session-1");
        historyDatabase.addEntry("create notes.txt", "session-1");
        historyDatabase.addEntry("write notes.txt hello", "session-1");
        historyDatabase.addEntry("read notes.txt", "session-1");

        List<String> recentHistory = historyDatabase.getRecentHistory(3);

        assertEquals(List.of("read notes.txt", "write notes.txt hello", "create notes.txt"), recentHistory);
    }

    @Test
    void getRecentHistoryReturnsEmptyListWhenLimitIsZero() {
        historyDatabase.addEntry("git status", "session-1");

        assertTrue(historyDatabase.getRecentHistory(0).isEmpty());
    }

    @Test
    void getRecentHistoryReturnsOnlyAvailableEntriesWhenLimitIsLarge() {
        historyDatabase.addEntry("git status", "session-1");
        historyDatabase.addEntry("create notes.txt", "session-1");

        List<String> recentHistory = historyDatabase.getRecentHistory(100);

        assertEquals(List.of("create notes.txt", "git status"), recentHistory);
    }

    @Test
    void searchHistoryReturnsOnlyMatchingGitCommands() {
        historyDatabase.addEntry("git status", "session-1");
        historyDatabase.addEntry("create notes.txt", "session-1");
        historyDatabase.addEntry("git commit -m init", "session-1");

        List<String> matches = historyDatabase.searchHistory("git");

        assertEquals(List.of("git commit -m init", "git status"), matches);
    }

    @Test
    void searchHistoryReturnsEmptyListForEmptyQuery() {
        historyDatabase.addEntry("git status", "session-1");

        assertTrue(historyDatabase.searchHistory("").isEmpty());
    }

    @Test
    void searchHistoryReturnsEmptyListForNullQuery() {
        historyDatabase.addEntry("git status", "session-1");

        assertTrue(historyDatabase.searchHistory(null).isEmpty());
    }

    @Test
    void clearHistoryDeletesEveryEntry() {
        historyDatabase.addEntry("git status", "session-1");
        historyDatabase.addEntry("create notes.txt", "session-1");

        historyDatabase.clearHistory();

        assertEquals(0, historyDatabase.getTotalCount());
    }

    @Test
    void getSessionHistoryReturnsOnlyEntriesForThatSessionInOriginalOrder() {
        historyDatabase.addEntry("git status", "session-1");
        historyDatabase.addEntry("create notes.txt", "session-2");
        historyDatabase.addEntry("git add .", "session-1");

        List<String> sessionHistory = historyDatabase.getSessionHistory("session-1");

        assertEquals(List.of("git status", "git add ."), sessionHistory);
    }

    @Test
    void getSessionHistoryReturnsEmptyListForBlankSessionId() {
        historyDatabase.addEntry("git status", "session-1");

        assertTrue(historyDatabase.getSessionHistory("  ").isEmpty());
    }

    @Test
    void getTotalCountReturnsZeroForEmptyDatabase() {
        assertEquals(0, historyDatabase.getTotalCount());
    }
}
