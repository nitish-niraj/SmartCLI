package com.lpu.smartcli.core;

import com.lpu.smartcli.data.FileSystem;

public class CreateCommand implements Command {

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length < 2) {
            ErrorHandler.missingArgument("create", "filename");
            return;
        }

        String filename = args[1];
        if (fs.fileExists(filename)) {
            ErrorHandler.fileAlreadyExists(filename);
            return;
        }

        fs.createFile(filename);
        System.out.println("[OK] File created: '" + filename + "'");
    }

    @Override
    public String getDescription() {
        return "create <filename>   — creates a new empty file";
    }
}
