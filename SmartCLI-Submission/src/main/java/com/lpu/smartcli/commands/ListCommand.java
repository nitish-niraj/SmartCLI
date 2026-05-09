package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

import java.util.List;

public class ListCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        List<String> files = fs.listFiles();
        if (files.isEmpty()) {
            System.out.println("No files found.");
            return;
        }

        System.out.println("Files in memory (" + files.size() + "):");
        for (String file : files) {
            System.out.println("- " + file);
        }
    }

    @Override
    public String getDescription() {
        return "list  — List all files in memory";
    }
}
