package com.lpu.smartcli.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private static CommandRegistry instance;
    private HashMap<String, Command> registry = new HashMap<>();

    private CommandRegistry() {
    }

    public static CommandRegistry getInstance() {
        if (instance == null) {
            instance = new CommandRegistry();
            instance.register("create", new CreateCommand());
            instance.register("write", new WriteCommand());
        }

        return instance;
    }

    public void register(String commandName, Command command) {
        registry.put(commandName, command);
    }

    public Command getCommand(String name) {
        return registry.get(name);
    }

    public List<String> getAllCommandNames() {
        return new ArrayList<>(registry.keySet());
    }

    public boolean hasCommand(String name) {
        return registry.containsKey(name);
    }

    public void printAll() {
        for (Map.Entry<String, Command> entry : registry.entrySet()) {
            System.out.println(entry.getKey() + " — " + entry.getValue().getDescription());
        }
    }
}
