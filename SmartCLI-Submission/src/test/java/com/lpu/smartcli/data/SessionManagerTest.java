package com.lpu.smartcli.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionManagerTest {
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errorOutput;
    private HistoryDatabase historyDatabase;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        errorOutput = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorOutput));
        historyDatabase = new HistoryDatabase(":memory:");
        sessionManager = new SessionManager(historyDatabase);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    void constructorThrowsWhenHistoryDatabaseIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SessionManager(null)
        );

        assertEquals("HistoryDatabase cannot be null", exception.getMessage());
    }

    @Test
    void getSessionIdReturnsValidUuid() {
        String sessionId = sessionManager.getSessionId();

        assertNotNull(sessionId);
        assertFalse(sessionId.isBlank());
        assertEquals(sessionId, UUID.fromString(sessionId).toString());
    }

    @Test
    void twoSessionManagersHaveDifferentSessionIds() {
        SessionManager otherSessionManager = new SessionManager(historyDatabase);

        assertNotEquals(sessionManager.getSessionId(), otherSessionManager.getSessionId());
    }

    @Test
    void getCurrentDirectoryStartsAtUserDir() {
        assertNotNull(sessionManager.getCurrentDirectory());
        assertEquals(System.getProperty("user.dir"), sessionManager.getCurrentDirectory());
    }

    @Test
    void changeDirectoryReturnsTrueForExistingDirectory() throws Exception {
        String homeDirectory = System.getProperty("user.home");

        boolean changed = sessionManager.changeDirectory(homeDirectory);

        assertTrue(changed);
        assertEquals(new File(homeDirectory).getCanonicalPath(), sessionManager.getCurrentDirectory());
    }

    @Test
    void changeDirectoryReturnsFalseForFakePathAndLeavesDirectoryUnchanged() {
        String originalDirectory = sessionManager.getCurrentDirectory();

        boolean changed = sessionManager.changeDirectory("Z:/does/not/exist/xyz");

        assertFalse(changed);
        assertEquals(originalDirectory, sessionManager.getCurrentDirectory());
    }

    @Test
    void changeDirectoryReturnsFalseForNullPath() {
        assertFalse(sessionManager.changeDirectory(null));
    }

    @Test
    void changeDirectoryReturnsFalseForBlankPath() {
        assertFalse(sessionManager.changeDirectory("   "));
    }

    @Test
    void recordCommandStoresCommandInSessionHistory() {
        sessionManager.recordCommand("git status");

        assertTrue(sessionManager.getSessionHistory().contains("git status"));
    }

    @Test
    void recordCommandWithNullDoesNothingAndDoesNotThrow() {
        assertDoesNotThrow(() -> sessionManager.recordCommand(null));

        assertTrue(sessionManager.getSessionHistory().isEmpty());
    }

    @Test
    void recordCommandWithEmptyStringDoesNothingAndDoesNotThrow() {
        assertDoesNotThrow(() -> sessionManager.recordCommand(""));

        assertTrue(sessionManager.getSessionHistory().isEmpty());
    }

    @Test
    void recordCommandStoresThreeCommands() {
        sessionManager.recordCommand("git status");
        sessionManager.recordCommand("create notes.txt");
        sessionManager.recordCommand("read notes.txt");

        assertEquals(3, sessionManager.getSessionHistory().size());
    }

    @Test
    void getSessionHistoryReturnsOnlyCommandsFromCurrentSession() {
        historyDatabase.addEntry("git status", "different-session");
        sessionManager.recordCommand("create notes.txt");
        sessionManager.recordCommand("read notes.txt");

        List<String> sessionHistory = sessionManager.getSessionHistory();

        assertEquals(List.of("create notes.txt", "read notes.txt"), sessionHistory);
        assertFalse(sessionHistory.contains("git status"));
    }

    @Test
    void getSessionDurationSecondsIsNeverNegative() {
        assertTrue(sessionManager.getSessionDurationSeconds() >= 0);
    }

    @Test
    void getSummaryContainsSessionIdAndCurrentDirectory() {
        String summary = sessionManager.getSummary();

        assertTrue(summary.contains(sessionManager.getSessionId()));
        assertTrue(summary.contains(sessionManager.getCurrentDirectory()));
    }
}
