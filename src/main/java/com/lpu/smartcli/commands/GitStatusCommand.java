package com.lpu.smartcli.commands;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.integration.GitIntegration;

public class GitStatusCommand implements Command {
    @Override
    public void execute(String[] args, FileSystem fs) {
        GitIntegration.GitStatus status = GitIntegration.getStatus(fs.getWorkingDirectory().toString());
        System.out.println("Git status for: " + fs.getWorkingDirectory());
        System.out.println("Modified : " + status.getModified());
        System.out.println("Staged   : " + status.getStaged());
        System.out.println("Untracked: " + status.getUntracked());
    }

    @Override
    public String getDescription() {
        return "gitstatus — Show Git status using JGit";
    }
}
