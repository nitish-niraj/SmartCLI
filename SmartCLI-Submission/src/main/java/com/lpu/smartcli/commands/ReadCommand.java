package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

public class ReadCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args.length == 0) {
            ErrorHandler.missingArgs("read <filename>");
            return;
        }

        String content = fs.readFile(args[0]);
        if (content == null) {
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
