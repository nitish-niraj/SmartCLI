package com.lpu.smartcli.data;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import java.io.FileNotFoundException;

public class ReadCommand implements Command {

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            ErrorHandler.missingArgs("read <filename>");
            return;
        }

        String filename = args[0];
        if (!fs.fileExists(filename)) {
            ErrorHandler.fileNotFound(filename);
            return;
        }

        try {
            String content = fs.readFile(filename);
            if (content.isEmpty()) {
                System.out.println("File '" + filename + "' is empty.");
            } else {
                System.out.println("--- " + filename + " ---");
                System.out.println(content);
                System.out.println("------------------");
            }
        } catch (FileNotFoundException e) {
            ErrorHandler.fileNotFound(filename);
        }
    }

    @Override
    public String getDescription() {
        return "read <filename> — displays the content of a file";
    }
}
