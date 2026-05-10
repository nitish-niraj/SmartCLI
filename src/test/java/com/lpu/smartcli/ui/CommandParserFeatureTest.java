package com.lpu.smartcli.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.storage.AliasStore;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandParserFeatureTest {
    private final PrintStream originalOut = System.out;
    private boolean originalAiEnabled;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        originalAiEnabled = CommandParser.AI_ENABLED;
        CommandParser.AI_ENABLED = false;
    }

    @AfterEach
    void tearDown() {
        CommandParser.AI_ENABLED = originalAiEnabled;
        System.setOut(originalOut);
    }

    @Test
    void parserExpandsAliasBeforeRouting() {
        ConfigManager config = new ConfigManager(tempDirectory.resolve("config.json"));
        new AliasStore(config).addAlias("mk", "create alias.txt");
        CommandParser parser = new CommandParser(config);
        FileSystem fs = new FileSystem();
        fs.setWorkingDirectory(tempDirectory);

        Command command = parser.parse("mk");
        assertNotNull(command);
        command.execute(parser.getArgs("mk"), fs);

        assertTrue(fs.fileExists("alias.txt"));
    }

    @Test
    void parserSuggestsAutocorrectForUnknownCommand() {
        ConfigManager config = new ConfigManager(tempDirectory.resolve("config.json"));
        CommandParser parser = new CommandParser(config);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Command command = parser.parse("craete hello.txt");

        assertNull(command);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Did you mean: create?"));
    }

    @Test
    void parserRegistersNewBuiltInCommands() {
        CommandParser parser = new CommandParser(new ConfigManager(tempDirectory.resolve("config.json")));

        assertTrue(parser.getRegistry().containsKey("alias"));
        assertTrue(parser.getRegistry().containsKey("theme"));
        assertTrue(parser.getRegistry().containsKey("gitstatus"));
    }

    @Test
    void parserUsesLocalNaturalLanguageFallbackWhenAiUnavailable() throws Exception {
        CommandParser.AI_ENABLED = true;
        CommandParser parser = new CommandParser(new ConfigManager(tempDirectory.resolve("config.json")));
        Field aiAvailable = CommandParser.class.getDeclaredField("aiAvailable");
        aiAvailable.setAccessible(true);
        aiAvailable.set(parser, false);

        FileSystem fs = new FileSystem();
        fs.setWorkingDirectory(tempDirectory);
        Command command = parser.parse("create a python file with name niraj.py");
        assertNotNull(command);
        command.execute(parser.getArgs("create a python file with name niraj.py"), fs);

        assertTrue(fs.fileExists("niraj.py"));
    }
}
