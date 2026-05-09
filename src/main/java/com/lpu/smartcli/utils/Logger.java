package com.lpu.smartcli.utils;

/**
 * Logger placeholder for centralized logging functionality.
 * Wraps SLF4J for consistent logging across the application.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class Logger {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Logger.class);

    /**
     * Logs an info level message.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning level message.
     *
     * @param message the message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs an error level message.
     *
     * @param message the message to log
     * @param exception the exception to log
     */
    public static void error(String message, Exception exception) {
        logger.error(message, exception);
    }

    /**
     * Logs a debug level message.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        logger.debug(message);
    }
}
