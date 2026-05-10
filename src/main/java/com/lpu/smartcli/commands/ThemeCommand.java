package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.ui.ThemeManager;

public class ThemeCommand implements Command {
    private final ThemeManager themeManager;

    public ThemeCommand(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            System.out.println("Current theme: " + themeManager.getTheme());
            return;
        }

        if ("switch".equalsIgnoreCase(args[0]) || "toggle".equalsIgnoreCase(args[0])) {
            System.out.println("Theme switched to: " + themeManager.switchTheme());
            return;
        }

        System.out.println("Usage: theme | theme switch");
    }

    @Override
    public String getDescription() {
        return "theme — Show or switch the saved terminal theme";
    }
}
