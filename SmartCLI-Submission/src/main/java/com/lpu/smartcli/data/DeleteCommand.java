package com.lpu.smartcli.data;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.ErrorHandler;
import java.util.Scanner;

public class DeleteCommand implements Command {

    @Override
    public void execute(String[] args, FileSystem fs) {
        if (args == null || args.length == 0 || args[0].isBlank()) {
            System.out.println("ERROR: Missing arguments. Usage: delete <filename>");
            return;
        }

        String filename = args[0];
        if (!fs.fileExists(filename)) {
            System.out.println("ERROR: File not found: " + filename);
            return;
        }

        System.out.print("Are you sure you want to delete '" + filename + "'? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine();

        if ("yes".equalsIgnoreCase(confirmation.trim())) {
            fs.deleteFile(filename);
            System.out.println("File '" + filename + "' deleted successfully.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    @Override
    public String getDescription() {
        return "delete <filename> — permanently removes a file";
    }
}
