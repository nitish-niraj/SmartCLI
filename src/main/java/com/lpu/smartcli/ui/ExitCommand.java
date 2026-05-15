package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.CommandExecutor;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;

public class ExitCommand implements Command {
    private final HistoryDatabase historyDatabase;
    private final ConfigManager configManager;
    private final FileSystem persistedFileSystem;
    private final SessionManager persistedSession;

    public ExitCommand() {
        this(null, null, null, null);
    }

    public ExitCommand(HistoryDatabase historyDatabase, ConfigManager configManager) {
        this(historyDatabase, configManager, null, null);
    }

    public ExitCommand(
            HistoryDatabase historyDatabase,
            ConfigManager configManager,
            FileSystem persistedFileSystem,
            SessionManager persistedSession
    ) {
        this.historyDatabase = historyDatabase;
        this.configManager = configManager;
        this.persistedFileSystem = persistedFileSystem;
        this.persistedSession = persistedSession;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (configManager != null && persistedFileSystem != null && persistedSession != null) {
            configManager.persistSessionState(persistedFileSystem, persistedSession);
        }
        if (configManager != null) {
            configManager.close();
        }
        if (historyDatabase != null) {
            historyDatabase.close();
        }
        CommandExecutor.shutdown();
        System.out.println("Exiting Smart CLI. Goodbye!");
        System.exit(0);
    }

    @Override
    public String getDescription() {
        return "exit — Exit the application";
    }
}
