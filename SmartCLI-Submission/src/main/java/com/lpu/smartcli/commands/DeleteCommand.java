package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class DeleteCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length == 0) {
            ErrorHandler.missingArgs("delete <filename>");
            return;
        }

        fs.deleteFile(args[0]);
    }

    @Override
    public String getDescription() {
        return "delete — Delete a file";
    }
}
