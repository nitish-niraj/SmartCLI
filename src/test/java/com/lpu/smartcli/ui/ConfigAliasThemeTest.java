package com.lpu.smartcli.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lpu.smartcli.storage.AliasStore;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigAliasThemeTest {
    @TempDir
    Path tempDirectory;

    @Test
    void configManagerCreatesJsonConfigWithDefaults() {
        Path configPath = tempDirectory.resolve("config.json");
        ConfigManager config = new ConfigManager(configPath);

        assertEquals("dark", config.getString("theme", ""));
        assertEquals(500, config.getInt("historyLimit", 0));
        assertTrue(configPath.toFile().exists());
    }

    @Test
    void themeSwitchPersistsToConfigFile() {
        Path configPath = tempDirectory.resolve("config.json");
        ConfigManager config = new ConfigManager(configPath);
        ThemeManager theme = new ThemeManager(config);

        String switched = theme.switchTheme();
        ConfigManager reloaded = new ConfigManager(configPath);

        assertEquals(switched, reloaded.getString("theme", ""));
    }

    @Test
    void aliasStoreAddsResolvesListsAndDeletesAlias() {
        ConfigManager config = new ConfigManager(tempDirectory.resolve("config.json"));
        AliasStore aliases = new AliasStore(config);

        aliases.addAlias("gs", "git status");

        assertEquals("git status", aliases.resolve("gs").orElseThrow());
        assertTrue(aliases.listAliases().containsKey("gs"));
        assertTrue(aliases.removeAlias("gs"));
        assertTrue(aliases.resolve("gs").isEmpty());
    }
}
