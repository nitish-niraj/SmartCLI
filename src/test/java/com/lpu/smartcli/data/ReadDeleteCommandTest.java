package com.lpu.smartcli.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReadDeleteCommandTest {
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream output;
    private FileSystem fileSystem;
    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        fileSystem = new FileSystem();
        fileSystem.setWorkingDirectory(tempDirectory);
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void readCommandPrintsFileContentWithHeader() {
        fileSystem.createFile("notes.txt");
        fileSystem.writeFile("notes.txt", "Hello World");
        ReadCommand command = new ReadCommand();

        command.execute(new String[]{"notes.txt"}, fileSystem);

        String printed = getOutput();
        assertTrue(printed.contains("--- notes.txt ---"));
        assertTrue(printed.contains("Hello World"));
        assertTrue(printed.contains("------------------"));
    }

    @Test
    void readCommandPrintsEmptyMessageForEmptyFile() {
        fileSystem.createFile("empty.txt");
        ReadCommand command = new ReadCommand();

        command.execute(new String[]{"empty.txt"}, fileSystem);

        assertTrue(getOutput().contains("File 'empty.txt' is empty."));
    }

    @Test
    void readCommandPrintsFileNotFoundWhenFileDoesNotExist() {
        ReadCommand command = new ReadCommand();

        command.execute(new String[]{"missing.txt"}, fileSystem);

        assertTrue(getOutput().contains("ERROR: File not found: missing.txt"));
    }

    @Test
    void readCommandPrintsMissingArgsWhenArgsIsNull() {
        ReadCommand command = new ReadCommand();

        command.execute(null, fileSystem);

        assertTrue(getOutput().contains("ERROR: Missing arguments. Usage: read <filename>"));
    }

    @Test
    void readCommandPrintsMissingArgsWhenArgsIsEmpty() {
        ReadCommand command = new ReadCommand();

        command.execute(new String[]{}, fileSystem);

        assertTrue(getOutput().contains("ERROR: Missing arguments. Usage: read <filename>"));
    }

    @Test
    void deleteCommandDeletesFileWhenUserTypesYes() {
        fileSystem.createFile("notes.txt");
        setInput("yes");
        DeleteCommand command = new DeleteCommand();

        command.execute(new String[]{"notes.txt"}, fileSystem);

        assertFalse(fileSystem.fileExists("notes.txt"));
        assertTrue(getOutput().contains("File 'notes.txt' deleted successfully."));
    }

    @Test
    void deleteCommandKeepsFileWhenUserTypesNo() {
        fileSystem.createFile("notes.txt");
        setInput("no");
        DeleteCommand command = new DeleteCommand();

        command.execute(new String[]{"notes.txt"}, fileSystem);

        assertTrue(fileSystem.fileExists("notes.txt"));
        assertTrue(getOutput().contains("Delete cancelled."));
    }

    @Test
    void deleteCommandPrintsFileNotFoundWhenFileDoesNotExist() {
        DeleteCommand command = new DeleteCommand();

        command.execute(new String[]{"missing.txt"}, fileSystem);

        assertTrue(getOutput().contains("ERROR: File not found: missing.txt"));
    }

    @Test
    void deleteCommandPrintsMissingArgsWhenArgsIsNull() {
        DeleteCommand command = new DeleteCommand();

        command.execute(null, fileSystem);

        assertTrue(getOutput().contains("ERROR: Missing arguments. Usage: delete <filename>"));
    }

    @Test
    void readAndDeleteDescriptionsMatchPhaseTwoText() {
        assertTrue(new ReadCommand().getDescription()
                .contains("read <filename> — displays the content of a file"));
        assertTrue(new DeleteCommand().getDescription()
                .contains("delete <filename> — permanently removes a file"));
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream((input + System.lineSeparator()).getBytes(StandardCharsets.UTF_8)));
    }

    private String getOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
