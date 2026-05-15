package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.integration.GitIntegration;

public class GitDiffCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        String pathFilter = null;
        if (args != null && args.length > 0) {
            pathFilter = String.join(" ", args).trim();
            if (pathFilter.isEmpty()) {
                pathFilter = null;
            }
        }

        String output = GitIntegration.getDiff(fs.getWorkingDirectory().toString(), pathFilter);
        System.out.println("Git diff (working tree vs HEAD) for: " + fs.getWorkingDirectory());
        System.out.println(output);
    }

    @Override
    public String getDescription() {
        return "gitdiff — Show unstaged diff vs HEAD (optional path under repo)";
    }
}
