package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

import java.nio.file.Files;
import java.nio.file.Path;

public class CdCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            System.out.println("Usage: cd path");
            return;
        }

        String pathText = String.join(" ", args).trim();
        Path path = Path.of(pathText);
        if (!path.isAbsolute()) {
            path = fs.getWorkingDirectory().resolve(path);
        }

        path = path.toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            System.out.println("Directory not found: " + pathText);
            return;
        }

        fs.setWorkingDirectory(path);
        System.out.println("Working directory changed to: " + fs.getWorkingDirectory());
    }

    @Override
    public String getDescription() {
        return "cd    — Change the current directory";
    }
}
