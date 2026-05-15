package com.lpu.smartcli.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lpu.smartcli.utils.AppLogger;

public class HistoryDatabase implements AutoCloseable {
    private final Connection connection;

    public HistoryDatabase() {
        this(defaultDbPath().toString());
    }

    public HistoryDatabase(String customDbPath) {
        try {
            if (!":memory:".equals(customDbPath)) {
                Path path = Path.of(customDbPath).toAbsolutePath().normalize();
                Path parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                customDbPath = path.toString();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + customDbPath);
            initialize();
            AppLogger.getLogger(HistoryDatabase.class).info("Opened SQLite history database at {}", customDbPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to open history database: " + e.getMessage(), e);
        }
    }

    public void addEntry(String commandText, String sessionId) {
        validateRequired(commandText, "Command text cannot be null or empty");
        validateRequired(sessionId, "Session ID cannot be null or empty");

        String sql = "INSERT INTO commands(command_text, timestamp, session_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, commandText);
            statement.setString(2, LocalDateTime.now().toString());
            statement.setString(3, sessionId);
            statement.executeUpdate();
            AppLogger.getLogger(HistoryDatabase.class).info("Saved history entry for session {}", sessionId);
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error saving history: {}", e.getMessage(), e);
        }
    }

    public List<String> getRecentHistory(int limit) {
        List<String> recent = new ArrayList<>();
        if (limit <= 0) {
            return recent;
        }

        String sql = "SELECT command_text FROM commands ORDER BY id DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    recent.add(resultSet.getString("command_text"));
                }
            }
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error reading history: {}", e.getMessage(), e);
        }

        return recent;
    }

    public List<String> searchHistory(String query) {
        List<String> matches = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return matches;
        }

        String sql = "SELECT command_text FROM commands WHERE command_text LIKE ? ORDER BY id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + query + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    matches.add(resultSet.getString("command_text"));
                }
            }
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error searching history: {}", e.getMessage(), e);
        }

        return matches;
    }

    public void clearHistory() {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM commands")) {
            statement.executeUpdate();
            AppLogger.getLogger(HistoryDatabase.class).info("Cleared history database");
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error clearing history: {}", e.getMessage(), e);
        }
    }

    public int getTotalCount() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM commands");
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error counting history: {}", e.getMessage(), e);
            return 0;
        }
    }

    public List<String> getSessionHistory(String sessionId) {
        List<String> sessionHistory = new ArrayList<>();
        if (sessionId == null || sessionId.isBlank()) {
            return sessionHistory;
        }

        String sql = "SELECT command_text FROM commands WHERE session_id = ? ORDER BY id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sessionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessionHistory.add(resultSet.getString("command_text"));
                }
            }
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error reading session history: {}", e.getMessage(), e);
        }

        return sessionHistory;
    }

    @Override
    public void close() {
        try {
            connection.close();
            AppLogger.getLogger(HistoryDatabase.class).info("Closed SQLite history database");
        } catch (SQLException e) {
            AppLogger.getLogger(HistoryDatabase.class).error("Error closing history database: {}", e.getMessage(), e);
        }
    }

    private void initialize() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS commands (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    command_text TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    session_id TEXT NOT NULL
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static Path defaultDbPath() {
        return Path.of(System.getProperty("user.home"), ".smartcli", "history.db");
    }
}
