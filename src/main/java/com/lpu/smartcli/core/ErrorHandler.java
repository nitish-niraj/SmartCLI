package com.lpu.smartcli.core;

/**
 * ErrorHandler provides centralized error handling and reporting.
 * Contains static methods for common error scenarios.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class ErrorHandler {

    /**
     * Handles unknown command error.
     *
     * @param commandName the name of the unknown command
     * @return error message
     */
    public static void unknownCommand(String cmd) {
        // TODO: Route to centralized logger and UI error presenter.
        System.out.println("Unknown command: " + cmd);
    }

    /**
     * Handles file not found error.
     *
     * @param filePath the path of the file that was not found
     * @return error message
     */
    public static void fileNotFound(String name) {
        // TODO: Use localization-ready error templates.
        System.out.println("File not found: " + name);
    }

    /**
     * Handles missing arguments error.
     *
     * @param commandName the command that has missing arguments
     * @param expectedArgs the expected arguments
     * @return error message
     */
    public static void missingArgs(String usage) {
        // TODO: Print command-specific usage with examples.
        System.out.println("Missing arguments. Usage: " + usage);
    }

    /**
     * Handles general execution error.
     *
     * @param commandName the command that failed
     * @param exception the exception that occurred
     * @return error message
     */
    public static void executionError(String detail) {
        // TODO: Attach error codes and telemetry metadata.
        System.out.println("Execution error: " + detail);
    }
}
