package com.lpu.smartcli.plugins;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Discovers {@link PluginAPI} implementations from {@code *.jar} files in a directory
 * using {@link ServiceLoader} and a dedicated {@link URLClassLoader}.
 */
public final class PluginLoader {

    private PluginLoader() {
    }

    /**
     * Loads every {@link PluginAPI} provider declared in plugin JARs under {@code pluginsDir}.
     * Non-existent directories are created empty.
     */
    public static List<PluginAPI> loadPlugins(Path pluginsDir) throws IOException {
        if (pluginsDir == null) {
            throw new IllegalArgumentException("pluginsDir cannot be null");
        }

        Path absolute = pluginsDir.toAbsolutePath().normalize();
        if (Files.notExists(absolute)) {
            Files.createDirectories(absolute);
        }

        if (!Files.isDirectory(absolute)) {
            return List.of();
        }

        List<URL> jarUrls = new ArrayList<>();
        try (var stream = Files.list(absolute)) {
            stream.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .sorted()
                    .forEach(path -> {
                        try {
                            jarUrls.add(path.toUri().toURL());
                        } catch (IOException ignored) {
                            System.err.println("[plugin] Skipping unreadable JAR: " + path);
                        }
                    });
        }

        if (jarUrls.isEmpty()) {
            return List.of();
        }

        URLClassLoader classLoader = URLClassLoader.newInstance(
                jarUrls.toArray(URL[]::new),
                PluginLoader.class.getClassLoader());

        List<PluginAPI> plugins = new ArrayList<>();
        ServiceLoader<PluginAPI> serviceLoader = ServiceLoader.load(PluginAPI.class, classLoader);
        for (PluginAPI plugin : serviceLoader) {
            plugins.add(plugin);
        }

        return plugins;
    }
}
