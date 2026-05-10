package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class WriteCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        try {
            if (args == null || args.length == 0) {
                System.out.println("Usage: write filename your content here");
                return;
            }

            int filenameIndex = "write".equalsIgnoreCase(args[0]) ? 1 : 0;
            int contentStartIndex = filenameIndex + 1;
            if (args.length <= contentStartIndex) {
                System.out.println("Usage: write filename your content here");
                return;
            }

            String filename = args[filenameIndex];
            if (!fs.fileExists(filename)) {
                ErrorHandler.fileNotFound(filename);
                return;
            }

            StringBuilder content = new StringBuilder();
            for (int i = contentStartIndex; i < args.length; i++) {
                if (i > contentStartIndex) {
                    content.append(" ");
                }
                content.append(args[i]);
            }

            fs.writeFile(filename, content.toString());
        } catch (Exception e) {
            ErrorHandler.executionError(e.getMessage() != null ? e.getMessage() : "Unknown write error");
        }
    }

    @Override
    public String getDescription() {
        return "write  — Write content to a file";
    }
}
