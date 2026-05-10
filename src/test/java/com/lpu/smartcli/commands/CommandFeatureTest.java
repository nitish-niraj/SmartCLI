package com.lpu.smartcli.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lpu.smartcli.data.FileSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFeatureTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;
    private FileSystem fs;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        fs = new FileSystem();
        fs.setWorkingDirectory(tempDirectory);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void activeDeleteCommandCancelsWhenUserDoesNotConfirm() {
        fs.createFile("notes.txt");
        DeleteCommand.setScanner(new Scanner(new ByteArrayInputStream("no\n".getBytes(StandardCharsets.UTF_8))));

        new DeleteCommand().execute(new String[]{"notes.txt"}, fs);

        assertTrue(fs.fileExists("notes.txt"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Delete cancelled."));
    }

    @Test
    void activeDeleteCommandDeletesWhenUserConfirms() {
        fs.createFile("notes.txt");
        DeleteCommand.setScanner(new Scanner(new ByteArrayInputStream("yes\n".getBytes(StandardCharsets.UTF_8))));

        new DeleteCommand().execute(new String[]{"notes.txt"}, fs);

        assertFalse(fs.fileExists("notes.txt"));
    }

    @Test
    void activeReadCommandPrintsEmptyFileMessage() {
        fs.createFile("empty.txt");

        new ReadCommand().execute(new String[]{"empty.txt"}, fs);

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("File 'empty.txt' is empty."));
    }
}
