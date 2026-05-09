package com.lpu.smartcli.core;

/**
 * PlatformDetector detects the operating system and provides platform-specific utilities.
 * Supports Windows, Linux, and macOS.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class PlatformDetector {

    /**
     * Enum for different operating system types.
     */
    public enum OSType {
        WINDOWS,
        LINUX,
        MAC,
        UNKNOWN
    }

    private static OSType detectedOS;

    static {
        detectedOS = detectOS();
    }

    /**
     * Detects the current operating system.
     *
     * @return the detected OSType
     * @todo Implement OS detection logic
     */
    public static OSType detectOS() {
        // TODO: Implement actual OS detection using System.getProperty("os.name")
        String osName = System.getProperty("os.name", "").toLowerCase();
        
        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux")) {
            return OSType.LINUX;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        }
        
        return OSType.UNKNOWN;
    }

    /**
     * Gets the current detected OS.
     *
     * @return the detected OSType
     */
    public static OSType getOS() {
        return detectedOS;
    }

    /**
     * Returns the shell prefix for the current platform.
     * Windows: cmd /c
     * Linux/Mac: sh -c
     *
     * @return the shell prefix command
     * @todo Implement shell prefix logic
     */
    public static String getShellPrefix() {
        // TODO: Return appropriate shell prefix based on OS
        return switch (detectedOS) {
            case WINDOWS -> "cmd /c";
            case LINUX, MAC -> "sh -c";
            case UNKNOWN -> "";
        };
    }

    /**
     * Checks if the current OS is Windows.
     *
     * @return true if OS is Windows, false otherwise
     */
    public static boolean isWindows() {
        return detectedOS == OSType.WINDOWS;
    }

    /**
     * Checks if the current OS is Linux.
     *
     * @return true if OS is Linux, false otherwise
     */
    public static boolean isLinux() {
        return detectedOS == OSType.LINUX;
    }

    /**
     * Checks if the current OS is macOS.
     *
     * @return true if OS is macOS, false otherwise
     */
    public static boolean isMac() {
        return detectedOS == OSType.MAC;
    }
}
