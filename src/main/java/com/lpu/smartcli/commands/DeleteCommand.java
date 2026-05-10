package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import com.lpu.smartcli.data.FileSystem;

import java.util.Scanner;

public class DeleteCommand implements Command {
    private static Scanner sharedScanner;

    public static void setScanner(Scanner scanner) {
        sharedScanner = scanner;
    }

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0) {
            ErrorHandler.missingArgs("delete <filename>");
            return;
        }

        if (!fs.fileExists(args[0])) {
            ErrorHandler.fileNotFound(args[0]);
            return;
        }

        Scanner scanner = sharedScanner == null ? new Scanner(System.in) : sharedScanner;
        System.out.print("Are you sure you want to delete '" + args[0] + "'? (yes/no): ");
        String answer = scanner.nextLine();
        if (!"yes".equalsIgnoreCase(answer.trim())) {
            System.out.println("Delete cancelled.");
            return;
        }

        fs.deleteFile(args[0]);
    }

    @Override
    public String getDescription() {
        return "delete — Delete a file";
    }
}
