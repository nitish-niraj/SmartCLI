package com.lpu.smartcli.storage;

import com.lpu.smartcli.ui.ConfigManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AliasStore {
    private final ConfigManager config;

    public AliasStore(ConfigManager config) {
        this.config = config;
    }

    public void addAlias(String name, String command) {
        validate(name, "Alias name cannot be empty");
        validate(command, "Alias command cannot be empty");
        Map<String, String> aliases = config.getAliases();
        aliases.put(name, command);
        config.set("aliases", aliases);
    }

    public boolean removeAlias(String name) {
        validate(name, "Alias name cannot be empty");
        Map<String, String> aliases = config.getAliases();
        boolean removed = aliases.remove(name) != null;
        config.set("aliases", aliases);
        return removed;
    }

    public Optional<String> resolve(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(config.getAliases().get(name));
    }

    public Map<String, String> listAliases() {
        return new LinkedHashMap<>(config.getAliases());
    }

    private void validate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
