package com.lpu.smartcli.ai;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class NvidiaAIClient {
    private static final String API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL = "minimaxai/minimax-m2.7";
    private static final String CONFIG_FILE = "config.properties";
    private static final String API_KEY_PROPERTY = "nvidia.api.key";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private static final String MISSING_CONFIG_MESSAGE =
            "ERROR: config.properties not found. Create it at project root with: nvidia.api.key=YOUR_KEY";
    private static final String SYSTEM_PROMPT =
            "You are a command interpreter for a file system CLI. Convert natural language to exactly one CLI command. "
                    + "Rules: 1) create filename — creates a file. "
                    + "2) write filename content — writes content to file, filename always comes BEFORE content. "
                    + "3) read filename — reads a file. "
                    + "4) delete filename — deletes a file. "
                    + "5) list — lists all files. "
                    + "6) help — shows help. "
                    + "If the user asks to do two things at once like create a file AND write to it, only return the FIRST operation. "
                    + "Never combine two commands. Never return two lines. Always return exactly one command. "
                    + "For create commands, the filename is always the last word that contains a dot extension like .py .txt .java. "
                    + "Ignore all other words like one, a, the, new. "
                    + "Reply with ONLY the command. No explanation. No quotes. No punctuation. "
                    + "Examples: user says make hello.py → create hello.py. "
                    + "User says write hello world to notes.txt → write notes.txt hello world. "
                    + "User says show notes.txt contents → read notes.txt.";
    private final String apiKey;

    public NvidiaAIClient() {
        apiKey = loadApiKey();
        if (apiKey == null) {
            throw new IllegalStateException(MISSING_CONFIG_MESSAGE);
        }
    }

    public String interpret(String userInput) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(REQUEST_TIMEOUT)
                    .build();
            String requestBody = buildRequestBody(userInput);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            String reply = extractAssistantContent(response.body());
            if (reply == null || reply.isBlank()) {
                return null;
            }

            return reply.trim();
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        NvidiaAIClient client = new NvidiaAIClient();
        String command = client.interpret("show me all my files");
        if (command != null) {
            System.out.println(command);
        }
    }

    private String loadApiKey() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            return null;
        }

        String apiKey = properties.getProperty(API_KEY_PROPERTY);
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        return apiKey.trim();
    }

    private String buildRequestBody(String userInput) {
        return "{"
                + "\"model\":\"" + escapeJson(MODEL) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(SYSTEM_PROMPT) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userInput) + "\"}"
                + "],"
                + "\"stream\":false,"
                + "\"max_tokens\":100"
                + "}";
    }

    private String extractAssistantContent(String json) {
        try {
            int choicesIndex = json.indexOf("\"choices\"");
            if (choicesIndex < 0) {
                return parseFailure();
            }

            int choicesArrayStart = json.indexOf('[', choicesIndex);
            if (choicesArrayStart < 0) {
                return parseFailure();
            }

            int firstChoiceStart = findFirstNonWhitespace(json, choicesArrayStart + 1);
            if (firstChoiceStart < 0 || json.charAt(firstChoiceStart) == ']') {
                return parseFailure();
            }

            int choicesArrayEnd = findMatchingBracket(json, choicesArrayStart, '[', ']');
            if (choicesArrayEnd < 0 || firstChoiceStart > choicesArrayEnd) {
                return parseFailure();
            }

            int messageIndex = json.indexOf("\"message\"", firstChoiceStart);
            if (messageIndex < 0 || messageIndex > choicesArrayEnd) {
                return parseFailure();
            }

            int messageObjectStart = json.indexOf('{', messageIndex);
            if (messageObjectStart < 0 || messageObjectStart > choicesArrayEnd) {
                return parseFailure();
            }

            int messageObjectEnd = findMatchingBracket(json, messageObjectStart, '{', '}');
            if (messageObjectEnd < 0 || messageObjectEnd > choicesArrayEnd) {
                return parseFailure();
            }

            int contentIndex = json.indexOf("\"content\"", messageObjectStart);
            if (contentIndex < 0 || contentIndex > messageObjectEnd) {
                return parseFailure();
            }

            int colonIndex = json.indexOf(':', contentIndex);
            if (colonIndex < 0 || colonIndex > messageObjectEnd) {
                return parseFailure();
            }

            int valueStart = findNextQuote(json, colonIndex + 1);
            if (valueStart < 0 || valueStart > messageObjectEnd) {
                return parseFailure();
            }

            String content = readJsonString(json, valueStart);
            if (content == null) {
                return parseFailure();
            }

            return content;
        } catch (Exception e) {
            return parseFailure();
        }
    }

    private String parseFailure() {
        return null;
    }

    private int findFirstNonWhitespace(String text, int startIndex) {
        for (int i = startIndex; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    private int findMatchingBracket(String text, int openIndex, char openChar, char closeChar) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = openIndex; i < text.length(); i++) {
            char current = text.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\' && inString) {
                escaped = true;
                continue;
            }

            if (current == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (current == openChar) {
                depth++;
            } else if (current == closeChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int findNextQuote(String text, int startIndex) {
        for (int i = startIndex; i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                return i;
            }
        }

        return -1;
    }

    private String readJsonString(String json, int quoteIndex) {
        StringBuilder value = new StringBuilder();

        for (int i = quoteIndex + 1; i < json.length(); i++) {
            char current = json.charAt(i);

            if (current == '"') {
                return value.toString();
            }

            if (current == '\\' && i + 1 < json.length()) {
                char escaped = json.charAt(++i);
                appendEscapedCharacter(value, escaped, json, i);
                if (escaped == 'u') {
                    i += 4;
                }
            } else {
                value.append(current);
            }
        }

        return null;
    }

    private void appendEscapedCharacter(StringBuilder value, char escaped, String json, int escapedIndex) {
        switch (escaped) {
            case '"':
                value.append('"');
                break;
            case '\\':
                value.append('\\');
                break;
            case '/':
                value.append('/');
                break;
            case 'b':
                value.append('\b');
                break;
            case 'f':
                value.append('\f');
                break;
            case 'n':
                value.append('\n');
                break;
            case 'r':
                value.append('\r');
                break;
            case 't':
                value.append('\t');
                break;
            case 'u':
                appendUnicodeEscape(value, json, escapedIndex + 1);
                break;
            default:
                value.append(escaped);
                break;
        }
    }

    private void appendUnicodeEscape(StringBuilder value, String json, int hexStart) {
        if (hexStart + 4 > json.length()) {
            return;
        }

        String hex = json.substring(hexStart, hexStart + 4);
        try {
            value.append((char) Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            value.append("\\u").append(hex);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            switch (current) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(current);
                    break;
            }
        }

        return escaped.toString();
    }
}
