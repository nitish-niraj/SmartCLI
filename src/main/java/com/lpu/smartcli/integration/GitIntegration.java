package com.lpu.smartcli.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;

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

        public Set<String> getModified() {
            return modified;
        }

        public Set<String> getStaged() {
            return staged;
        }

        public Set<String> getUntracked() {
            return untracked;
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

    /**
     * Returns a concise one-line-per-commit log for {@code HEAD}, newest first.
     *
     * @param repoPath working tree path containing {@code .git}
     * @param maxCount maximum commits to include (clamped to at least 1)
     */
    public static String getLog(String repoPath, int maxCount) {
        int limit = Math.max(1, Math.min(maxCount, 500));
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            try (repository; Git git = Git.wrap(repository)) {
                Iterable<RevCommit> commits = git.log().setMaxCount(limit).call();
                StringBuilder output = new StringBuilder();
                for (RevCommit commit : commits) {
                    String shortId = commit.getName().substring(0, Math.min(7, commit.getName().length()));
                    output.append(shortId)
                            .append("  ")
                            .append(firstLine(commit.getFullMessage()))
                            .append(System.lineSeparator());
                }

                if (output.length() == 0) {
                    return "(empty repository — no commits yet)";
                }

                return output.toString().trim();
            }
        } catch (Exception e) {
            return "[ERROR] Git log: " + e.getMessage();
        }
    }

    /**
     * Returns a unified diff of the working tree versus {@code HEAD} (unstaged changes).
     *
     * @param repoPath working tree path containing {@code .git}
     * @param pathFilter optional single path filter relative to repo root; blank for entire tree
     */
    public static String getDiff(String repoPath, String pathFilter) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            try (repository) {
                ObjectId headId = repository.resolve("HEAD");
                if (headId == null) {
                    return "(no commits yet — nothing to diff against)";
                }

                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                try (ObjectReader reader = repository.newObjectReader();
                        RevWalk walk = new RevWalk(repository)) {
                    RevCommit headCommit = walk.parseCommit(headId);
                    oldTree.reset(reader, headCommit.getTree());
                }

                FileTreeIterator newTree = new FileTreeIterator(repository);
                Git git = Git.wrap(repository);
                var diffCommand = git.diff().setOldTree(oldTree).setNewTree(newTree);
                if (pathFilter != null && !pathFilter.isBlank()) {
                    diffCommand.setPathFilter(PathFilter.create(pathFilter.replace('\\', '/')));
                }

                List<DiffEntry> entries = diffCommand.call();
                if (entries.isEmpty()) {
                    return "(no unstaged changes vs HEAD)";
                }

                return formatDiff(repository, entries);
            }
        } catch (Exception e) {
            return "[ERROR] Git diff: " + e.getMessage();
        }
    }

    private static String formatDiff(Repository repository, List<DiffEntry> entries) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(buffer)) {
            formatter.setRepository(repository);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            formatter.setDetectRenames(true);
            for (DiffEntry entry : entries) {
                formatter.format(entry);
            }
        }

        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private static String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        int newline = message.indexOf('\n');
        return newline >= 0 ? message.substring(0, newline).trim() : message.trim();
    }

    public static void main(String[] args) {
        GitStatus status = getStatus(".");
        System.out.println(status);
        System.out.println("Git Integration Phase 7 complete");
    }
}
