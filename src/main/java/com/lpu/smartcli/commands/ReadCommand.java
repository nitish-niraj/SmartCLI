package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class ReadCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            ErrorHandler.missingArgs("read <filename>");
            return;
        }

        if (!fs.fileExists(args[0])) {
            ErrorHandler.fileNotFound(args[0]);
            return;
        }

        String content = fs.readFile(args[0]);
        if (content == null) {
            return;
        }

        if (content.isEmpty()) {
            System.out.println("File '" + args[0] + "' is empty.");
            return;
        }

        System.out.println("--- " + args[0] + " ---");
        System.out.println(content);
        System.out.println("-----------------");
    }

    @Override
    public String getDescription() {
        return "read   — Read and display a file";
    }
}
