package com.lpu.smartcli.core;

public class Main {
    public static void main(String[] args) {
        CommandRegistry.getInstance().printAll();
        System.out.println("Smart CLI is ready. All systems operational.");
    }
}
