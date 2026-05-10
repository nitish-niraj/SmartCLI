package com.lpu.smartcli.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteHistoryFeatureTest {
    @TempDir
    Path tempDirectory;

    @Test
    void historyPersistsToSqliteFile() {
        Path dbPath = tempDirectory.resolve("history.db");
        try (HistoryDatabase first = new HistoryDatabase(dbPath.toString())) {
            first.addEntry("git status", "session-1");
        }

        try (HistoryDatabase second = new HistoryDatabase(dbPath.toString())) {
            assertEquals(1, second.getTotalCount());
            assertEquals("git status", second.getRecentHistory(1).get(0));
        }
    }
}
