package com.lpu.smartcli.core;

public class PlatformDetector {

    public enum OSType {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    public static OSType detectOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))   return OSType.WINDOWS;
        if (os.contains("mac"))   return OSType.MAC;
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return OSType.LINUX;
        return OSType.UNKNOWN;
    }

    public static String[] getShellPrefix() {
        return detectOS() == OSType.WINDOWS
            ? new String[]{"cmd.exe", "/c"}
            : new String[]{"/bin/sh", "-c"};
    }

    public static void printInfo() {
        System.out.println("Detected OS : " + detectOS());
        System.out.println("Shell prefix: " + java.util.Arrays.toString(getShellPrefix()));
    }

    public static void main(String[] args) {
        printInfo();
    }
}
