package com.lpu.smartcli.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IntegrationTest {

    @Test
    void fileSystemBrowserConstructorThrowsForNullRootPath() {
        assertThrows(IllegalArgumentException.class, () -> new FileSystemBrowser(null, 1));
    }

    @Test
    void fileSystemBrowserConstructorThrowsForFakePath() {
        assertThrows(IllegalArgumentException.class, () -> new FileSystemBrowser("Z:/does/not/exist/xyz", 1));
    }

    @Test
    void fileSystemBrowserConstructorThrowsForZeroMaxDepth() {
        assertThrows(IllegalArgumentException.class, () -> new FileSystemBrowser(System.getProperty("user.dir"), 0));
    }

    @Test
    void buildTreeCreatesDirectoryRootForHomeDirectory() {
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.home"), 1);

        FileSystemBrowser.TreeNode root = browser.buildTree();

        assertTrue(root.isDirectory);
        assertFalse(root.name.isBlank());
    }

    @Test
    void buildTreeInitializesChildrenList() {
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.home"), 1);

        FileSystemBrowser.TreeNode root = browser.buildTree();

        assertNotNull(root.children);
    }

    @Test
    void listFilesFlatReturnsOnlyFiles() {
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.dir"), 1);

        List<String> files = browser.listFilesFlat(System.getProperty("user.dir"));

        assertTrue(files.stream().allMatch(path -> new File(path).isFile()));
    }

    @Test
    void listDirectoriesFlatReturnsOnlyDirectories() {
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.dir"), 1);

        List<String> directories = browser.listDirectoriesFlat(System.getProperty("user.dir"));

        assertTrue(directories.stream().allMatch(path -> new File(path).isDirectory()));
    }

    @Test
    void listFilesFlatReturnsEmptyListForFakePath() {
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.dir"), 1);

        assertTrue(browser.listFilesFlat("Z:/does/not/exist/xyz").isEmpty());
    }

    @Test
    void renderTreeContainsBranchCharactersForSmallTree() {
        FileSystemBrowser.TreeNode root = new FileSystemBrowser.TreeNode("root", "/root", true);
        root.addChild(new FileSystemBrowser.TreeNode("child.txt", "/root/child.txt", false));
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.dir"), 1);

        String rendered = browser.renderTree(root, "");

        assertTrue(rendered.contains("└──") || rendered.contains("├──"));
    }

    @Test
    void renderTreeOnLeafReturnsOnlyNodeText() {
        FileSystemBrowser.TreeNode leaf = new FileSystemBrowser.TreeNode("child.txt", "/root/child.txt", false);
        FileSystemBrowser browser = new FileSystemBrowser(System.getProperty("user.dir"), 1);

        assertTrue(browser.renderTree(leaf, "").equals(leaf.toString()));
    }

    @Test
    void getAllProcessesReturnsNonEmptyList() {
        ProcessManager processManager = new ProcessManager();

        assertFalse(processManager.getAllProcesses().isEmpty());
    }

    @Test
    void getAllProcessesReturnsPositivePids() {
        ProcessManager processManager = new ProcessManager();

        assertTrue(processManager.getAllProcesses().stream().allMatch(process -> process.pid > 0));
    }

    @Test
    void getProcessCountReturnsPositiveCount() {
        ProcessManager processManager = new ProcessManager();

        assertTrue(processManager.getProcessCount() > 0);
    }

    @Test
    void searchByNameJavaDoesNotThrow() {
        ProcessManager processManager = new ProcessManager();

        assertDoesNotThrow(() -> processManager.searchByName("java"));
    }

    @Test
    void searchByNameReturnsEmptyListForNullName() {
        ProcessManager processManager = new ProcessManager();

        assertTrue(processManager.searchByName(null).isEmpty());
    }

    @Test
    void searchByNameReturnsEmptyListForBlankName() {
        ProcessManager processManager = new ProcessManager();

        assertTrue(processManager.searchByName("").isEmpty());
    }

    @Test
    void killProcessReturnsFalseForInvalidPid() {
        ProcessManager processManager = new ProcessManager();

        assertFalse(processManager.killProcess(-1));
    }

    @Test
    void killProcessReturnsFalseForMissingPid() {
        ProcessManager processManager = new ProcessManager();

        assertFalse(processManager.killProcess(999999999L));
    }

    @Test
    void findByPidReturnsCurrentJvmProcess() {
        ProcessManager processManager = new ProcessManager();
        long selfPid = ProcessHandle.current().pid();

        Optional<ProcessManager.ProcessInfo> processInfo = processManager.findByPid(selfPid);

        assertTrue(processInfo.isPresent());
    }

    @Test
    void findByPidReturnsEmptyForMissingPid() {
        ProcessManager processManager = new ProcessManager();

        assertTrue(processManager.findByPid(999999999L).isEmpty());
    }
}
