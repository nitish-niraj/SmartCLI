package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;

/**
 * ExitCommand placeholder for terminal exit functionality.
 * Implementation to be added in Phase 1.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public class ExitCommand implements Command {

    /**
     * Executes the exit command.
     *
     * @param args the command line arguments
     * @param fs   the file system context
     * @todo Implement exit command execution
     * @todo Save session data before exiting
     * @todo Cleanup resources
     */
    @Override
    public void execute(String[] args, FileSystem fs) {
        System.out.println("Exiting Smart CLI. Goodbye!");
        System.exit(0);
    }

    /**
     * Returns a description of the exit command.
     *
     * @return the command description
     */
    @Override
    public String getDescription() {
        return "exit — Exit the application";
    }
}
