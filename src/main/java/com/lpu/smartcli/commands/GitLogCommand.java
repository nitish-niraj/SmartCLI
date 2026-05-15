package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.integration.GitIntegration;

public class GitLogCommand implements Command {
    private static final int DEFAULT_LIMIT = 20;

    @Override
    public void execute(String[] args, FileSystem fs) {
        int limit = DEFAULT_LIMIT;
        if (args != null && args.length > 0) {
            try {
                limit = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException e) {
                System.out.println("Usage: gitlog [maxCommits]");
                return;
            }
        }

        String output = GitIntegration.getLog(fs.getWorkingDirectory().toString(), limit);
        System.out.println("Git log (max " + limit + ") for: " + fs.getWorkingDirectory());
        System.out.println(output);
    }

    @Override
    public String getDescription() {
        return "gitlog — Show recent Git commits (optional: max count)";
    }
}
