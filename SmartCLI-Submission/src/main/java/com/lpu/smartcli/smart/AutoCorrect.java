package com.lpu.smartcli.smart;

import com.lpu.smartcli.core.Command;
import com.lpu.smartcli.core.CommandRegistry;
import com.lpu.smartcli.data.FileSystem;

import java.util.List;
import java.util.Optional;

public class AutoCorrect {

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],
                            Math.min(dp[i][j - 1], dp[i - 1][j - 1])
                    );
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    public static Optional<String> suggest(String input, List<String> knownCommands) {
        String bestMatch = null;
        int lowestDistance = Integer.MAX_VALUE;

        for (String command : knownCommands) {
            int distance = levenshtein(input, command);
            if (distance < lowestDistance) {
                lowestDistance = distance;
                bestMatch = command;
            }
        }

        if (lowestDistance <= 2) {
            return Optional.of(bestMatch);
        }

        return Optional.empty();
    }

    public static Optional<String> suggest(String input) {
        List<String> knownCommands = CommandRegistry.getInstance().getAllCommandNames();
        return suggest(input, knownCommands);
    }

    public static void main(String[] args) {
        CommandRegistry.getInstance().register("git", new Command() {
            @Override
            public void execute(String[] args, FileSystem fs) {
            }

            @Override
            public String getDescription() {
                return "git — Git command";
            }
        });

        printSuggestion(suggest("gti"));
        printSuggestion(suggest("craete"));
        printSuggestion(suggest("xyz"));
    }

    private static void printSuggestion(Optional<String> suggestion) {
        if (suggestion.isPresent()) {
            System.out.println("Did you mean: " + suggestion.get());
        } else {
            System.out.println("No suggestion found");
        }
    }
}
