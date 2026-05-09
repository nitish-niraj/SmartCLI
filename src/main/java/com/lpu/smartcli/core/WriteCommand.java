package com.lpu.smartcli.core;

import com.lpu.smartcli.data.FileSystem;

public class WriteCommand implements Command {

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length < 3) {
            ErrorHandler.missingArgs("write <filename> <content>");
            return;
        }

        String filename = args[1];
        if (!fs.fileExists(filename)) {
            ErrorHandler.fileNotFound(filename);
            return;
        }

        String content = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        fs.writeFile(filename, content);
        System.out.println("[OK] Written to '" + filename + "'");
    }

    @Override
    public String getDescription() {
        return "write <filename> <content>   — writes content to a file";
    }
}
