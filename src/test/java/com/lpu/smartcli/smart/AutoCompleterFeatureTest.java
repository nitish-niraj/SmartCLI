package com.lpu.smartcli.smart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lpu.smartcli.commands.CreateCommand;
import com.lpu.smartcli.core.Command;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AutoCompleterFeatureTest {
    @TempDir
    Path tempDirectory;

    @Test
    void suggestsFilesFromSmartCliWorkingDirectory() throws Exception {
        Files.writeString(tempDirectory.resolve("alpha.txt"), "hello");
        Map<String, Command> registry = new LinkedHashMap<>();
        registry.put("create", new CreateCommand());
        AutoCompleter completer = new AutoCompleter(registry, tempDirectory);

        assertTrue(completer.suggestFile("al").contains("alpha.txt"));
    }

    @Test
    void completesKnownGitSubcommands() {
        Map<String, Command> registry = new LinkedHashMap<>();
        registry.put("create", new CreateCommand());
        AutoCompleter completer = new AutoCompleter(registry, tempDirectory);

        assertTrue(completer.complete("git sta").contains("status"));
    }
}
