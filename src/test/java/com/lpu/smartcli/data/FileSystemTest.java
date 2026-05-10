package com.lpu.smartcli.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemTest {
    private FileSystem fileSystem;
    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        fileSystem = new FileSystem();
        fileSystem.setWorkingDirectory(tempDirectory);
    }

    @Test
    void createFileAddsNewEmptyFile() throws FileNotFoundException {
        fileSystem.createFile("notes.txt");

        assertTrue(fileSystem.fileExists("notes.txt"));
        assertEquals("", fileSystem.readFile("notes.txt"));
    }

    @Test
    void createFileThrowsWhenDuplicateExists() {
        fileSystem.createFile("notes.txt");

        fileSystem.createFile("notes.txt");

        assertEquals(1, fileSystem.getFileCount());
    }

    @Test
    void createFileThrowsWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileSystem.createFile(null)
        );
        assertEquals("File name cannot be null or empty", exception.getMessage());
    }

    @Test
    void readFileReturnsWrittenContent() throws FileNotFoundException {
        fileSystem.createFile("report.txt");
        fileSystem.writeFile("report.txt", "CAP477 project");

        assertEquals("CAP477 project", fileSystem.readFile("report.txt"));
    }

    @Test
    void readFileThrowsWhenFileDoesNotExist() {
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileSystem.readFile("missing.txt")
        );
        assertEquals("File not found: missing.txt", exception.getMessage());
    }

    @Test
    void readFileReturnsEmptyStringForEmptyFile() throws FileNotFoundException {
        fileSystem.createFile("empty.txt");

        assertEquals("", fileSystem.readFile("empty.txt"));
    }

    @Test
    void writeFileUpdatesExistingFile() throws FileNotFoundException {
        fileSystem.createFile("notes.txt");

        fileSystem.writeFile("notes.txt", "hello");

        assertEquals("hello", fileSystem.readFile("notes.txt"));
    }

    @Test
    void writeFileThrowsWhenFileDoesNotExist() {
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileSystem.writeFile("missing.txt", "hello")
        );
        assertEquals("File not found: missing.txt", exception.getMessage());
    }

    @Test
    void writeFileTreatsNullContentAsEmptyString() throws FileNotFoundException {
        fileSystem.createFile("notes.txt");

        fileSystem.writeFile("notes.txt", null);

        assertEquals("", fileSystem.readFile("notes.txt"));
    }

    @Test
    void deleteFileRemovesExistingFile() throws FileNotFoundException {
        fileSystem.createFile("delete-me.txt");

        fileSystem.deleteFile("delete-me.txt");

        assertFalse(fileSystem.fileExists("delete-me.txt"));
    }

    @Test
    void deleteFileThrowsWhenFileDoesNotExist() {
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileSystem.deleteFile("missing.txt")
        );
        assertEquals("File not found: missing.txt", exception.getMessage());
    }

    @Test
    void deleteFileThrowsWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileSystem.deleteFile("   ")
        );
        assertEquals("File name cannot be null or empty", exception.getMessage());
    }

    @Test
    void fileExistsReturnsTrueWhenFileExists() {
        fileSystem.createFile("notes.txt");

        assertTrue(fileSystem.fileExists("notes.txt"));
    }

    @Test
    void fileExistsReturnsFalseWhenFileDoesNotExist() {
        assertFalse(fileSystem.fileExists("missing.txt"));
    }

    @Test
    void fileExistsReturnsFalseForNullInput() {
        assertFalse(fileSystem.fileExists(null));
    }

    @Test
    void listFilesReturnsAllFiles() {
        fileSystem.createFile("one.txt");
        fileSystem.createFile("two.txt");

        List<String> files = fileSystem.listFiles();

        assertEquals(2, files.size());
        assertTrue(files.contains("one.txt"));
        assertTrue(files.contains("two.txt"));
    }

    @Test
    void listFilesReturnsEmptyListWhenNoFilesExist() {
        assertTrue(fileSystem.listFiles().isEmpty());
    }

    @Test
    void listFilesReturnsCopyNotInternalMapView() {
        fileSystem.createFile("notes.txt");
        List<String> files = fileSystem.listFiles();

        files.clear();

        assertTrue(fileSystem.fileExists("notes.txt"));
        assertEquals(1, fileSystem.getFileCount());
    }

    @Test
    void getFileCountReturnsNumberOfCreatedFiles() {
        fileSystem.createFile("one.txt");
        fileSystem.createFile("two.txt");

        assertEquals(2, fileSystem.getFileCount());
    }

    @Test
    void getFileCountReturnsZeroWhenNoFilesExist() {
        assertEquals(0, fileSystem.getFileCount());
    }

    @Test
    void getFileCountDecreasesAfterDelete() throws FileNotFoundException {
        fileSystem.createFile("one.txt");
        fileSystem.createFile("two.txt");

        fileSystem.deleteFile("one.txt");

        assertEquals(1, fileSystem.getFileCount());
    }

    @Test
    void clearAllRemovesEveryFile() {
        fileSystem.createFile("one.txt");
        fileSystem.createFile("two.txt");

        fileSystem.clearAll();

        assertEquals(0, fileSystem.getFileCount());
        assertTrue(fileSystem.listFiles().isEmpty());
    }

    @Test
    void clearAllWorksWhenMapIsAlreadyEmpty() {
        fileSystem.clearAll();

        assertEquals(0, fileSystem.getFileCount());
    }

    @Test
    void clearAllAllowsSameNamesToBeCreatedAgain() {
        fileSystem.createFile("notes.txt");
        fileSystem.clearAll();

        fileSystem.createFile("notes.txt");

        assertTrue(fileSystem.fileExists("notes.txt"));
        assertEquals(1, fileSystem.getFileCount());
    }

    @Test
    void afterDeleteFileIsNotListedAndDoesNotExist() throws FileNotFoundException {
        fileSystem.createFile("keep.txt");
        fileSystem.createFile("remove.txt");

        fileSystem.deleteFile("remove.txt");

        assertFalse(fileSystem.fileExists("remove.txt"));
        assertFalse(fileSystem.listFiles().contains("remove.txt"));
        assertTrue(fileSystem.listFiles().contains("keep.txt"));
    }
}
