package com.lpu.smartcli.ui;

public class ThemeManager {
    private final ConfigManager config;

    public ThemeManager(ConfigManager config) {
        this.config = config;
    }

    public String getTheme() {
        return config.getString("theme", "dark");
    }

    public String switchTheme() {
        String nextTheme = "dark".equalsIgnoreCase(getTheme()) ? "light" : "dark";
        config.set("theme", nextTheme);
        return nextTheme;
    }

    public String colorCommand(String text) {
        if ("light".equalsIgnoreCase(getTheme())) {
            return "\u001B[34m" + text + "\u001B[0m";
        }

        return "\u001B[96m" + text + "\u001B[0m";
    }
}
