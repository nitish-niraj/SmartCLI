package com.lpu.smartcli.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager implements AutoCloseable {
    private static final Type CONFIG_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private final Map<String, Object> values;

    public ConfigManager() {
        this(defaultConfigPath());
    }

    public ConfigManager(Path configPath) {
        this.configPath = configPath.toAbsolutePath().normalize();
        this.values = load();
    }

    public Object get(String key) {
        return values.get(key);
    }

    public String getString(String key, String defaultValue) {
        Object value = values.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void set(String key, Object value) {
        values.put(key, value);
        save();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getAliases() {
        Object aliases = values.get("aliases");
        if (!(aliases instanceof Map<?, ?> map)) {
            Map<String, String> emptyAliases = new LinkedHashMap<>();
            values.put("aliases", emptyAliases);
            return emptyAliases;
        }

        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                normalized.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }

        values.put("aliases", normalized);
        return (Map<String, String>) values.get("aliases");
    }

    public Path getConfigPath() {
        return configPath;
    }

    public void save() {
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(configPath, gson.toJson(values));
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        save();
    }

    private Map<String, Object> load() {
        try {
            if (Files.notExists(configPath)) {
                Map<String, Object> defaults = defaults();
                Path parent = configPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(configPath, gson.toJson(defaults));
                return defaults;
            }

            String json = Files.readString(configPath);
            Map<String, Object> loaded = gson.fromJson(json, CONFIG_TYPE);
            if (loaded == null) {
                loaded = new LinkedHashMap<>();
            }

            Map<String, Object> merged = defaults();
            merged.putAll(loaded);
            return merged;
        } catch (Exception e) {
            System.out.println("Error loading config, using defaults: " + e.getMessage());
            return defaults();
        }
    }

    private static Map<String, Object> defaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("theme", "dark");
        defaults.put("historyLimit", 500);
        defaults.put("aliases", new LinkedHashMap<String, String>());
        return defaults;
    }

    private static Path defaultConfigPath() {
        return Path.of(System.getProperty("user.home"), ".smartcli", "config.json");
    }
}
