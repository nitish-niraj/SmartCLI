package com.lpu.smartcli.core;

import com.lpu.smartcli.data.FileSystem;

/**
 * Command interface defines the contract for all executable commands in SmartCLI.
 * All command implementations must implement this interface.
 *
 * @author SmartCLI Team
 * @version 1.0.0
 */
public interface Command {

    /**
     * Executes the command with the given arguments and file system context.
     *
     * @param args the command line arguments
     * @param fs   the file system context for file operations
     */
    void execute(String[] args, FileSystem fs);

    /**
     * Returns a description of the command.
     *
     * @return the command description
     */
    String getDescription();
}
