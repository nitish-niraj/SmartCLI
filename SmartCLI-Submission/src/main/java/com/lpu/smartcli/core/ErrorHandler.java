package com.lpu.smartcli.core;

public class ErrorHandler {
    public static void unknownCommand(String input) {
        System.out.println("Unknown command: '" + input + "'. Type 'help' to see all commands.");
    }

    public static void missingArgs(String usage) {
        System.out.println("Missing arguments. Usage: " + usage);
    }

    public static void fileNotFound(String filename) {
        System.out.println("Error: File '" + filename + "' not found.");
    }

    public static void alreadyExists(String filename) {
        System.out.println("Error: File '" + filename + "' already exists.");
    }

    public static void executionError(String message) {
        System.out.println("Execution error: " + message);
    }
}
