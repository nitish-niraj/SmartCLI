package com.lpu.smartcli.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logger accessor.
 */
public final class AppLogger {
    private AppLogger() {}

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
