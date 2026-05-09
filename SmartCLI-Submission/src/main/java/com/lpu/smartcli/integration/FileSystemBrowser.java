package com.lpu.smartcli.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileSystemBrowser {
    private String rootPath;
    private int maxDepth;

    public static class TreeNode {
        public final String name;
        public final String absolutePath;
        public final boolean isDirectory;
        public final List<TreeNode> children;

        public TreeNode(String name, String absolutePath, boolean isDirectory) {
            this.name = name;
            this.absolutePath = absolutePath;
            this.isDirectory = isDirectory;
            this.children = new ArrayList<>();
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
        }

        @Override
        public String toString() {
            return (isDirectory ? "[DIR]  " : "[FILE] ") + name;
        }
    }

    public FileSystemBrowser(String rootPath, int maxDepth) {
        if (rootPath == null || rootPath.isBlank()) {
            throw new IllegalArgumentException("rootPath cannot be null or empty");
        }

        File root = new File(rootPath);
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException("Not a valid directory: " + rootPath);
        }

        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be >= 1");
        }

        this.rootPath = root.getAbsolutePath();
        this.maxDepth = maxDepth;
    }

    public TreeNode buildTree() {
        return buildTree(new File(rootPath), 1);
    }

    public List<String> listFilesFlat(String directoryPath) {
        File[] files = getVisibleChildren(directoryPath);
        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .map(File::getAbsolutePath)
                .toList();
    }

    public List<String> listDirectoriesFlat(String directoryPath) {
        File[] files = getVisibleChildren(directoryPath);
        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .filter(File::isDirectory)
                .sorted(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .map(File::getAbsolutePath)
                .toList();
    }

    public String renderTree(TreeNode node, String indent) {
        StringBuilder builder = new StringBuilder();
        builder.append(node);
        appendChildren(builder, node, indent == null ? "" : indent);
        return builder.toString();
    }

    public String getRootPath() {
        return rootPath;
    }

    private TreeNode buildTree(File file, int depth) {
        TreeNode node = new TreeNode(file.getName(), file.getAbsolutePath(), file.isDirectory());

        if (!file.isDirectory() || depth > maxDepth) {
            return node;
        }

        File[] children = file.listFiles();
        if (children == null) {
            return node;
        }

        Arrays.stream(children)
                .filter(child -> !child.getName().startsWith("."))
                .sorted(fileComparator())
                .forEach(child -> node.addChild(buildTree(child, depth + 1)));

        return node;
    }

    private void appendChildren(StringBuilder builder, TreeNode node, String indent) {
        for (int i = 0; i < node.children.size(); i++) {
            TreeNode child = node.children.get(i);
            boolean isLast = i == node.children.size() - 1;
            String branch = isLast ? "└── " : "├── ";

            builder.append(System.lineSeparator())
                    .append(indent)
                    .append(branch)
                    .append(child);

            String childIndent = indent + (isLast ? "    " : "│   ");
            appendChildren(builder, child, childIndent);
        }
    }

    private File[] getVisibleChildren(String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            return null;
        }

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles(file -> !file.getName().startsWith("."));
        if (files == null) {
            return null;
        }

        return files;
    }

    private Comparator<File> fileComparator() {
        return Comparator.comparing(File::isFile)
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
