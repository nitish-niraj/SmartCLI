package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

public class PwdCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        System.out.println("Current directory: " + fs.getWorkingDirectory());
    }

    @Override
    public String getDescription() {
        return "pwd   — Print the current directory";
    }
}
