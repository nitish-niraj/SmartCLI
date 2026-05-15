package com.lpu.smartcli.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.data.FileSystem;
import com.lpu.smartcli.data.HistoryDatabase;
import com.lpu.smartcli.data.SessionManager;
import com.lpu.smartcli.integration.ProcessManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TerminalPane extends Application {
    private static FileSystem sharedFileSystem;
    private static SessionManager sharedSession;
    private static HistoryDatabase sharedHistory;
    private static ConfigManager sharedConfig;

    private TextArea output;
    private TextField input;
    private javafx.scene.control.Label promptLabel;
    private HBox promptBox;
    private VBox shellTabContent;
    private VBox processTabContent;
    private VBox logsTabContent;
    private TabPane tabPane;
    private CommandParser parser;
    private com.lpu.smartcli.smart.AutoCompleter guiAutoCompleter;
    private TableView<ProcessManager.ProcessInfo> processTable;
    private TextArea logsArea;
    private final ProcessManager processManager = new ProcessManager();
    private final DateTimeFormatter startedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private final Path logFilePath = Path.of(System.getProperty("user.home"), ".smartcli", "smartcli.log");

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
        parser = new CommandParser(session, history, config, fs);
        guiAutoCompleter = new com.lpu.smartcli.smart.AutoCompleter(parser.getRegistry(), fs.getWorkingDirectory());

        output = new TextArea();
        output.setEditable(false);
        // Terminal-like behaviour: do not wrap so horizontal scroll appears
        output.setWrapText(false);
        output.setPrefRowCount(24);
        output.appendText("SmartCLI GUI mode\n");
        output.appendText("Type commands below. Current directory: " + fs.getWorkingDirectory() + "\n");
        output.appendText("Tip: use the Process Monitor tab to inspect or stop processes.\n\n");
        output.appendText("Logs are saved to: " + logFilePath + "\n\n");

        input = new TextField();
        input.setPromptText("");
        input.setOnAction(event -> {
            String rawInput = input.getText();
            input.clear();
            executeGuiCommand(rawInput, fs, session);
        });

        // Tab to complete / autocorrect like a real terminal
        // Use an event filter so we intercept Tab before JavaFX focus traversal.
        input.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, evt -> {
            try {
                if (evt.getCode() == javafx.scene.input.KeyCode.TAB) {
                    String current = input.getText();
                    String completed = guiAutoCompleter.complete(current);
                    if (completed != null && !completed.equals(current)) {
                        input.setText(completed);
                        input.positionCaret(completed.length());
                    }
                    evt.consume();
                }
            } catch (Exception ignored) {
            }
        });

        // Add a persistent prompt label next to the input to look like a real terminal
        promptLabel = new javafx.scene.control.Label("smartcli>");
        promptBox = new HBox(6, promptLabel, input);
        promptBox.setPadding(new Insets(6, 6, 6, 6));
        HBox.setHgrow(input, Priority.ALWAYS);

        shellTabContent = new VBox(8, output, promptBox);
        shellTabContent.setPadding(new Insets(10));
        VBox.setVgrow(output, Priority.ALWAYS);

        Tab shellTab = new Tab("Shell", shellTabContent);
        shellTab.setClosable(false);

        processTable = new TableView<>();
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ProcessManager.ProcessInfo, Long> pidColumn = new TableColumn<>("PID");
        pidColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().pid));

        TableColumn<ProcessManager.ProcessInfo, String> commandColumn = new TableColumn<>("Command");
        commandColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().command));

        TableColumn<ProcessManager.ProcessInfo, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().user));

        TableColumn<ProcessManager.ProcessInfo, String> startedColumn = new TableColumn<>("Started");
        startedColumn.setCellValueFactory(cell -> {
            long epoch = cell.getValue().startTimeEpochSecond;
            String text = epoch > 0 ? startedFormatter.format(Instant.ofEpochSecond(epoch)) : "?";
            return new ReadOnlyObjectWrapper<>(text);
        });

        processTable.getColumns().setAll(List.of(pidColumn, commandColumn, userColumn, startedColumn));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshProcessTable());

        Button killButton = new Button("Kill selected");
        killButton.setOnAction(event -> killSelectedProcess());

        HBox processToolbar = new HBox(8, refreshButton, killButton);
        processToolbar.setPadding(new Insets(0, 0, 8, 0));

        processTabContent = new VBox(8, processToolbar, processTable);
        processTabContent.setPadding(new Insets(10));
        VBox.setVgrow(processTable, Priority.ALWAYS);

        Tab processTab = new Tab("Process Monitor", processTabContent);
        processTab.setClosable(false);

        logsArea = new TextArea();
        logsArea.setEditable(false);
        logsArea.setWrapText(false);
        logsArea.setPrefRowCount(24);

        Button refreshLogsButton = new Button("Refresh logs");
        refreshLogsButton.setOnAction(event -> refreshLogs());

        HBox logsToolbar = new HBox(8, refreshLogsButton);
        logsToolbar.setPadding(new Insets(0, 0, 8, 0));

        logsTabContent = new VBox(8, logsToolbar, logsArea);
        logsTabContent.setPadding(new Insets(10));
        VBox.setVgrow(logsArea, Priority.ALWAYS);

        Tab logsTab = new Tab("Logs", logsTabContent);
        logsTab.setClosable(false);

        tabPane = new TabPane(shellTab, processTab, logsTab);
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldIndex, newIndex) -> {
            if (newIndex != null) {
                config.set("activeGuiTabIndex", newIndex.intValue());
            }
            if (newIndex != null && newIndex.intValue() == 1 && processTable.getItems().isEmpty()) {
                refreshProcessTable();
            }
            if (newIndex != null && newIndex.intValue() == 2) {
                refreshLogs();
            }
        });

        int savedTab = config.getInt("activeGuiTabIndex", 0);
        int safeIndex = Math.min(Math.max(savedTab, 0), tabPane.getTabs().size() - 1);
        tabPane.getSelectionModel().select(safeIndex);

        applyThemeStyles(config.getString("theme", "dark"));

        Scene scene = new Scene(tabPane, 920, 580);
        // Ctrl+Tab / Ctrl+Shift+Tab to switch between tabs like a real terminal app
        scene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            try {
                if (evt.isControlDown() && evt.getCode() == KeyCode.TAB) {
                    int current = tabPane.getSelectionModel().getSelectedIndex();
                    int size = tabPane.getTabs().size();
                    if (evt.isShiftDown()) {
                        // previous
                        int prev = (current - 1 + size) % size;
                        tabPane.getSelectionModel().select(prev);
                    } else {
                        // next
                        int next = (current + 1) % size;
                        tabPane.getSelectionModel().select(next);
                    }
                    evt.consume();
                }
            } catch (Exception ignored) {
            }
        });
        stage.setTitle("SmartCLI GUI");
        stage.setScene(scene);
        stage.show();
        // Ensure the input field has keyboard focus so Tab completion works immediately
        input.requestFocus();
        // Run help once on startup so the user sees available commands in the GUI output
        try {
            executeGuiCommand("help", fs, session);
        } catch (Exception ignored) {
        }
        refreshLogs();
    }

    private void refreshProcessTable() {
        List<ProcessManager.ProcessInfo> snapshot = processManager.getAllProcesses().stream()
                .limit(250)
                .toList();
        processTable.setItems(FXCollections.observableArrayList(snapshot));
    }

    private void killSelectedProcess() {
        ProcessManager.ProcessInfo selected = processTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        long pid = selected.pid;
        if (pid == ProcessHandle.current().pid()) {
            output.appendText("[gui] Refusing to kill the running SmartCLI JVM.\n");
            return;
        }

        boolean destroyed = processManager.killProcess(pid);
        if (!destroyed) {
            output.appendText("[gui] Could not signal PID " + pid + ". Try kill " + pid + " --force in Shell.\n");
        } else {
            output.appendText("[gui] Sent destroy to PID " + pid + " (" + selected.command + ")\n");
        }

        refreshProcessTable();
    }

    private void executeGuiCommand(String rawInput, FileSystem fs, SessionManager session) {
        if (rawInput == null || rawInput.isBlank()) {
            return;
        }

        output.appendText("smartcli> " + rawInput + "\n");
        session.recordCommand(rawInput);
        String beforeTheme = parser == null ? "dark" : sharedConfig.getString("theme", "dark");
        String normalizedInput = applyAutocomplete(rawInput);
        if (!normalizedInput.equals(rawInput)) {
            output.appendText("[autocorrect] " + rawInput + " -> " + normalizedInput + "\n");
        }

        Command command = parser.parse(normalizedInput);
        if (command == null) {
            output.appendText("[gui] Unknown command. Use Tab to complete or check the Logs tab.\n");
            output.appendText("\n");
            return;
        }

        if (command instanceof com.lpu.smartcli.commands.OsCommand) {
            com.lpu.smartcli.commands.OsCommand.setGuiOutputConsumers(this::appendOutputLine, this::appendOutputLine);
        } else {
            com.lpu.smartcli.commands.OsCommand.setGuiOutputConsumers(null, null);
        }

        String[] args = parser.getArgs(normalizedInput);
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);

        boolean isOsCommand = command instanceof com.lpu.smartcli.commands.OsCommand;

        try {
            if (!isOsCommand) {
                System.setOut(captureStream);
                System.setErr(captureStream);
            }

            command.execute(args, fs);
        } catch (Exception e) {
            captureStream.println("Execution error: " + (e.getMessage() == null ? "Unknown error" : e.getMessage()));
        } finally {
            if (!isOsCommand) {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }

        if (!isOsCommand) {
            output.appendText(captured.toString(StandardCharsets.UTF_8));
            output.appendText("\n");
        } else {
            output.appendText("[gui] OS command started. Output will stream here.\n\n");
        }

        String afterTheme = sharedConfig.getString("theme", "dark");
        if (!afterTheme.equalsIgnoreCase(beforeTheme)) {
            applyThemeStyles(afterTheme);
        }

        refreshLogs();
    }

    private String applyAutocomplete(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return rawInput;
        }

        String[] parts = rawInput.trim().split("\\s+", 2);
        if (parts.length == 0) {
            return rawInput;
        }

        String firstToken = parts[0];
        java.util.Optional<String> suggestion = parser.suggestCommandName(firstToken);
        if (suggestion.isPresent() && !suggestion.get().equalsIgnoreCase(firstToken)) {
            return parts.length == 1 ? suggestion.get() : suggestion.get() + " " + parts[1];
        }

        return rawInput;
    }

    private void appendOutputLine(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        Platform.runLater(() -> {
            output.appendText(text);
            if (!text.endsWith("\n")) {
                output.appendText("\n");
            }
            output.appendText("\n");
        });
    }

    private void refreshLogs() {
        if (logsArea == null) {
            return;
        }

        try {
            if (Files.exists(logFilePath)) {
                logsArea.setText(Files.readString(logFilePath));
            } else {
                logsArea.setText("Log file not found yet: " + logFilePath + "\nRun a few commands and refresh again.");
            }
        } catch (IOException e) {
            logsArea.setText("Could not read log file: " + e.getMessage());
        }
    }

    private void applyThemeStyles(String theme) {
        String mode = theme == null ? "dark" : theme.trim().toLowerCase();
        boolean light = "light".equals(mode);
        String monoFont = "Consolas, 'Courier New', monospace";

        String outputBackground = light ? "#f5f5f5" : "#000000";
        String outputText = light ? "#111111" : "#00FF9F";
        String inputBackground = light ? "#ffffff" : "#111111";
        String inputText = light ? "#111111" : "#ffffff";
        String promptText = light ? "#444444" : "#aaaaaa";
        String tabBackground = light ? "#ececec" : "#1b1b1b";
        String tabText = light ? "#111111" : "#f0f0f0";
        String controlBorder = light ? "#bcbcbc" : "#2c2c2c";

        output.setStyle("-fx-font-family: " + monoFont + "; -fx-font-size: 12px;"
                + " -fx-control-inner-background: " + outputBackground + ";"
                + " -fx-background-color: " + outputBackground + ";"
                + " -fx-text-fill: " + outputText + ";");

        input.setStyle("-fx-font-family: " + monoFont + "; -fx-font-size: 12px;"
                + " -fx-control-inner-background: " + inputBackground + ";"
                + " -fx-background-color: " + inputBackground + ";"
                + " -fx-text-fill: " + inputText + ";"
                + " -fx-prompt-text-fill: #888888;");

        if (promptLabel != null) {
            promptLabel.setStyle("-fx-font-family: " + monoFont + "; -fx-font-size: 12px;"
                    + " -fx-text-fill: " + promptText + "; -fx-padding: 4 8 4 4;");
        }

        if (promptBox != null) {
            promptBox.setStyle("-fx-background-color: " + tabBackground + "; -fx-border-color: " + controlBorder + ";");
        }

        if (shellTabContent != null) {
            shellTabContent.setStyle("-fx-background-color: " + tabBackground + ";");
        }

        if (processTabContent != null) {
            processTabContent.setStyle("-fx-background-color: " + tabBackground + ";");
        }

        if (tabPane != null) {
            tabPane.setStyle("-fx-background-color: " + tabBackground + "; -fx-border-color: " + controlBorder + ";");
        }
    }
}
