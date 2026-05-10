package com.lpu.smartcli.ui;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class TerminalPane extends Application {
    private static FileSystem sharedFileSystem;
    private static SessionManager sharedSession;
    private static HistoryDatabase sharedHistory;
    private static ConfigManager sharedConfig;

    private TextArea output;
    private TextField input;
    private CommandParser parser;

    public static void launchGui(FileSystem fs, SessionManager session, HistoryDatabase history, ConfigManager config) {
        sharedFileSystem = fs;
        sharedSession = session;
        sharedHistory = history;
        sharedConfig = config;
        Application.launch(TerminalPane.class);
    }

    @Override
    public void start(Stage stage) {
        FileSystem fs = sharedFileSystem == null ? new FileSystem() : sharedFileSystem;
        SessionManager session = sharedSession == null ? new SessionManager() : sharedSession;
        HistoryDatabase history = sharedHistory == null ? new HistoryDatabase() : sharedHistory;
        ConfigManager config = sharedConfig == null ? new ConfigManager() : sharedConfig;
        parser = new CommandParser(session, history, config);

        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setPrefRowCount(24);
        output.appendText("SmartCLI GUI mode\n");
        output.appendText("Type commands below. Current directory: " + fs.getWorkingDirectory() + "\n\n");

        input = new TextField();
        input.setPromptText("smartcli> ");
        input.setOnAction(event -> {
            String rawInput = input.getText();
            input.clear();
            executeGuiCommand(rawInput, fs, session);
        });

        VBox root = new VBox(8, output, input);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 900, 560);
        stage.setTitle("SmartCLI GUI");
        stage.setScene(scene);
        stage.show();
    }

    private void executeGuiCommand(String rawInput, FileSystem fs, SessionManager session) {
        if (rawInput == null || rawInput.isBlank()) {
            return;
        }

        output.appendText("smartcli> " + rawInput + "\n");
        session.recordCommand(rawInput);
        Command command = parser.parse(rawInput);
        if (command == null) {
            output.appendText("\n");
            return;
        }

        String[] args = parser.getArgs(rawInput);
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);

        try {
            System.setOut(captureStream);
            System.setErr(captureStream);
            command.execute(args, fs);
        } catch (Exception e) {
            captureStream.println("Execution error: " + (e.getMessage() == null ? "Unknown error" : e.getMessage()));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        Platform.runLater(() -> {
            output.appendText(captured.toString(StandardCharsets.UTF_8));
            output.appendText("\n");
        });
    }
}
