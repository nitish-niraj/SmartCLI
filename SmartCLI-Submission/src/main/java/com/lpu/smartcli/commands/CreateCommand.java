package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class CreateCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length == 0 || args[0].isBlank()) {
            ErrorHandler.missingArgs("create <filename>");
            return;
        }

        fs.createFile(args[0]);
    }

    @Override
    public String getDescription() {
        return "create — Create a new file";
    }
}
