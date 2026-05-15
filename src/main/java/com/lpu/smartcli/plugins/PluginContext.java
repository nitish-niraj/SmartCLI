package com.lpu.smartcli.plugins;

import com.lpu.smartcli.core.Command;

import java.nio.file.Path;

/**
 * Host callbacks exposed to plugins when they register commands.
 */
public interface PluginContext {

    /**
     * Registers a command name mapped to a {@link Command} implementation.
     * Duplicate names are ignored with a console warning.
     */
    void registerCommand(String name, Command command);

    /**
     * Directory scanned for {@code *.jar} plugin bundles.
     */
    Path getPluginsDirectory();
}
