package com.lpu.smartcli.plugins;

import com.lpu.smartcli.core.Command;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Coordinates plugin discovery and registration into the active {@link Command} map.
 */
public class PluginManager {
    private final Path pluginsDirectory;

    public PluginManager(Path pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory == null
                ? Path.of(System.getProperty("user.home"), ".smartcli", "plugins")
                : pluginsDirectory.toAbsolutePath().normalize();
    }

    public Path getPluginsDirectory() {
        return pluginsDirectory;
    }

    /**
     * Loads plugins from disk and registers their commands into {@code registry}.
     * Existing command names are never overridden.
     */
    public void registerPlugins(Map<String, Command> registry) {
        if (registry == null) {
            throw new IllegalArgumentException("registry cannot be null");
        }

        PluginContext context = new PluginContext() {
            @Override
            public void registerCommand(String name, Command command) {
                if (name == null || name.isBlank() || command == null) {
                    return;
                }

                String key = name.trim().toLowerCase();
                if (registry.containsKey(key)) {
                    System.out.println("[plugin] Command '" + key + "' already registered — skipping plugin override.");
                    return;
                }

                registry.put(key, command);
            }

            @Override
            public Path getPluginsDirectory() {
                return pluginsDirectory;
            }
        };

        try {
            List<PluginAPI> plugins = PluginLoader.loadPlugins(pluginsDirectory);
            if (plugins.isEmpty()) {
                return;
            }

            System.out.println("[plugin] Loading " + plugins.size() + " plugin(s) from " + pluginsDirectory);
            for (PluginAPI plugin : plugins) {
                PluginMetadata meta = plugin.metadata();
                System.out.println("[plugin] Initializing " + meta);
                try {
                    plugin.register(context);
                } catch (Throwable throwable) {
                    System.err.println("[plugin] Failed to start " + meta.getId() + ": " + throwable.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[plugin] Unable to load plugins: " + e.getMessage());
        }
    }
}
