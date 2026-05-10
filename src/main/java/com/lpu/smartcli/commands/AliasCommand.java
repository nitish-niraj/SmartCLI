package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.storage.AliasStore;

import java.util.Map;

public class AliasCommand implements Command {
    private final AliasStore aliasStore;

    public AliasCommand(AliasStore aliasStore) {
        this.aliasStore = aliasStore;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                addAlias(args);
                break;
            case "delete":
            case "remove":
                deleteAlias(args);
                break;
            case "list":
                listAliases();
                break;
            default:
                printUsage();
                break;
        }
    }

    @Override
    public String getDescription() {
        return "alias — Create, list, or delete command aliases";
    }

    private void addAlias(String[] args) {
        if (args.length < 3) {
            ErrorHandler.missingArgs("alias add <name> <command>");
            return;
        }

        String name = args[1];
        String command = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        aliasStore.addAlias(name, command);
        System.out.println("Alias '" + name + "' saved as: " + command);
    }

    private void deleteAlias(String[] args) {
        if (args.length < 2) {
            ErrorHandler.missingArgs("alias delete <name>");
            return;
        }

        if (aliasStore.removeAlias(args[1])) {
            System.out.println("Alias '" + args[1] + "' deleted.");
        } else {
            System.out.println("Alias not found: " + args[1]);
        }
    }

    private void listAliases() {
        Map<String, String> aliases = aliasStore.listAliases();
        if (aliases.isEmpty()) {
            System.out.println("No aliases configured.");
            return;
        }

        System.out.println("Aliases:");
        aliases.forEach((name, command) -> System.out.println("  " + name + " = " + command));
    }

    private void printUsage() {
        System.out.println("Usage: alias add <name> <command> | alias list | alias delete <name>");
    }
}
