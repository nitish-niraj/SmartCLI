package com.lpu.smartcli.integration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitIntegration {

    public static class GitStatus {
        Set<String> modified;
        Set<String> staged;
        Set<String> untracked;

        public GitStatus(Set<String> modified, Set<String> staged, Set<String> untracked) {
            this.modified = modified;
            this.staged = staged;
            this.untracked = untracked;
        }

        @Override
        public String toString() {
            return "Modified: " + modified
                    + System.lineSeparator() + "Staged: " + staged
                    + System.lineSeparator() + "Untracked: " + untracked;
        }
    }

    public static GitStatus getStatus(String repoPath) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            try (repository; Git git = Git.wrap(repository)) {
                Status status = git.status().call();
                Set<String> modified = status.getModified();
                Set<String> staged = status.getAdded();
                Set<String> untracked = status.getUntracked();

                return new GitStatus(modified, staged, untracked);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Git: " + e.getMessage());
            return new GitStatus(new HashSet<>(), new HashSet<>(), new HashSet<>());
        }
    }

    public static void main(String[] args) {
        GitStatus status = getStatus(".");
        System.out.println(status);
        System.out.println("Git Integration Phase 7 complete");
    }
}
