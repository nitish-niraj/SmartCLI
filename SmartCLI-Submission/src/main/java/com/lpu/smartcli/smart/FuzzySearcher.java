package com.lpu.smartcli.smart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FuzzySearcher {

    public static class SearchResult {
        public final String command;
        public final int score;

        public SearchResult(String command, int score) {
            this.command = command;
            this.score = score;
        }

        @Override
        public String toString() {
            return "[" + score + "] " + command;
        }
    }

    public boolean fuzzyMatch(String query, String candidate) {
        if (query == null || query.isBlank() || candidate == null || candidate.isBlank()) {
            return false;
        }

        String lowerQuery = query.toLowerCase();
        String lowerCandidate = candidate.toLowerCase();
        int queryIndex = 0;

        for (int i = 0; i < lowerCandidate.length() && queryIndex < lowerQuery.length(); i++) {
            if (lowerCandidate.charAt(i) == lowerQuery.charAt(queryIndex)) {
                queryIndex++;
            }
        }

        return queryIndex == lowerQuery.length();
    }

    public int matchScore(String query, String candidate) {
        if (!fuzzyMatch(query, candidate)) {
            return 0;
        }

        List<Integer> matchedIndices = getMatchedIndices(query.toLowerCase(), candidate.toLowerCase());
        int consecutivePairs = 0;

        for (int i = 0; i < matchedIndices.size() - 1; i++) {
            if (matchedIndices.get(i + 1) == matchedIndices.get(i) + 1) {
                consecutivePairs++;
            }
        }

        return consecutivePairs + 1;
    }

    public List<String> search(String query, List<String> history) {
        if (query == null || query.isBlank() || history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchResult> scoredResults = searchWithScores(query, history);
        List<String> commands = new ArrayList<>();

        for (SearchResult result : scoredResults) {
            commands.add(result.command);
        }

        return commands;
    }

    public List<SearchResult> searchWithScores(String query, List<String> history) {
        if (query == null || query.isBlank() || history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();

        for (String entry : history) {
            if (fuzzyMatch(query, entry)) {
                results.add(new SearchResult(entry, matchScore(query, entry)));
            }
        }

        results.sort(Comparator.comparingInt((SearchResult result) -> result.score)
                .reversed()
                .thenComparing((SearchResult result) -> result.command, Comparator.reverseOrder()));

        if (results.size() > 10) {
            return new ArrayList<>(results.subList(0, 10));
        }

        return results;
    }

    private List<Integer> getMatchedIndices(String query, String candidate) {
        List<Integer> matchedIndices = new ArrayList<>();
        int candidateIndex = 0;

        for (int queryIndex = 0; queryIndex < query.length(); queryIndex++) {
            char queryCharacter = query.charAt(queryIndex);

            while (candidateIndex < candidate.length()) {
                if (candidate.charAt(candidateIndex) == queryCharacter) {
                    matchedIndices.add(candidateIndex);
                    candidateIndex++;
                    break;
                }
                candidateIndex++;
            }
        }

        return matchedIndices;
    }
}
