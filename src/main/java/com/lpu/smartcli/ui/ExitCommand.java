package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.CommandExecutor;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;

public class ExitCommand implements Command {
    private final HistoryDatabase historyDatabase;
    private final ConfigManager configManager;

    public ExitCommand() {
        this(null, null);
    }

    public ExitCommand(HistoryDatabase historyDatabase, ConfigManager configManager) {
        this.historyDatabase = historyDatabase;
        this.configManager = configManager;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
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
