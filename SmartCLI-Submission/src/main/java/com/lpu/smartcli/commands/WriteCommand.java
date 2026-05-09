package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class WriteCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length < 2) {
            ErrorHandler.missingArgs("write <filename> <content>");
            return;
        }

        if (!fs.fileExists(args[0])) {
            ErrorHandler.fileNotFound(args[0]);
            return;
        }

        StringBuilder content = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                content.append(" ");
            }
            content.append(args[i]);
        }

        fs.writeFile(args[0], content.toString());
    }

    @Override
    public String getDescription() {
        return "write  — Write content to a file";
    }
}
