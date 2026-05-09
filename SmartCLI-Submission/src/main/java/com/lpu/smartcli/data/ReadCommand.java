package com.lpu.smartcli.data;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;

public class ReadCommand implements Command {

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            System.out.println("ERROR: Missing arguments. Usage: read <filename>");
            return;
        }

        String filename = args[0];
        if (!fs.fileExists(filename)) {
            System.out.println("ERROR: File not found: " + filename);
            return;
        }

        String content = fs.readFile(filename);
        if (content.isEmpty()) {
            System.out.println("File '" + filename + "' is empty.");
        } else {
            System.out.println("--- " + filename + " ---");
            System.out.println(content);
            System.out.println("------------------");
        }
    }

    @Override
    public String getDescription() {
        return "read <filename> — displays the content of a file";
    }
}
