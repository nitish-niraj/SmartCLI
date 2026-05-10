package com.lpu.smartcli.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandExecutorFeatureTest {
    @TempDir
    Path tempDirectory;

    @Test
    void executeAsyncReturnsCompletableFutureWithExitCodeAndTimestamp() throws Exception {
        CompletableFuture<CommandResult> future = CommandExecutor.executeAsync(
                "echo async-test",
                tempDirectory,
                line -> {
                },
                line -> {
                }
        );

        CommandResult result = future.get(10, TimeUnit.SECONDS);

        assertEquals(0, result.getExitCode());
        assertTrue(result.getStdout().contains("async-test"));
        assertNotNull(result.getTimestamp());
        assertTrue(result.isSuccess());
    }

    @Test
    void executeCapturesFailingCommandExitCode() {
        CommandResult result = CommandExecutor.execute("definitely_missing_smartcli_command_xyz");

        assertFalse(result.isSuccess());
    }
}
