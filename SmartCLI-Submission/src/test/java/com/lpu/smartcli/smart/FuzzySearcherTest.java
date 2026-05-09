package com.lpu.smartcli.smart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FuzzySearcherTest {
    private FuzzySearcher fuzzySearcher;

    @BeforeEach
    void setUp() {
        fuzzySearcher = new FuzzySearcher();
    }

    @Test
    void fuzzyMatchReturnsTrueForGitStatus() {
        assertTrue(fuzzySearcher.fuzzyMatch("gst", "git status"));
    }

    @Test
    void fuzzyMatchReturnsTrueForGitCommit() {
        assertTrue(fuzzySearcher.fuzzyMatch("gc", "git commit"));
    }

    @Test
    void fuzzyMatchReturnsFalseWhenCharactersAreNotInOrder() {
        assertFalse(fuzzySearcher.fuzzyMatch("gst", "git commit"));
    }

    @Test
    void fuzzyMatchReturnsTrueForExactMatch() {
        assertTrue(fuzzySearcher.fuzzyMatch("gs", "gs"));
    }

    @Test
    void fuzzyMatchReturnsFalseForEmptyQuery() {
        assertFalse(fuzzySearcher.fuzzyMatch("", "git status"));
    }

    @Test
    void fuzzyMatchReturnsFalseForNullQuery() {
        assertFalse(fuzzySearcher.fuzzyMatch(null, "git status"));
    }

    @Test
    void fuzzyMatchReturnsFalseForNullCandidate() {
        assertFalse(fuzzySearcher.fuzzyMatch("gst", null));
    }

    @Test
    void fuzzyMatchReturnsFalseForEmptyCandidate() {
        assertFalse(fuzzySearcher.fuzzyMatch("gst", ""));
    }

    @Test
    void fuzzyMatchReturnsFalseForMissingCharacters() {
        assertFalse(fuzzySearcher.fuzzyMatch("zz", "git status"));
    }

    @Test
    void fuzzyMatchIsCaseInsensitive() {
        assertTrue(fuzzySearcher.fuzzyMatch("GST", "git status"));
    }

    @Test
    void fuzzyMatchReturnsTrueForNonContiguousOrderedCharacters() {
        assertTrue(fuzzySearcher.fuzzyMatch("abc", "aXbXc"));
    }

    @Test
    void matchScoreRewardsContiguousCharactersInGitStatus() {
        assertTrue(fuzzySearcher.matchScore("gst", "git status") >= 2);
    }

    @Test
    void matchScoreReturnsOneForNonContiguousGitCommitMatch() {
        assertEquals(1, fuzzySearcher.matchScore("gc", "git commit"));
    }

    @Test
    void matchScoreRewardsFullyContiguousGitPrefix() {
        assertTrue(fuzzySearcher.matchScore("git", "git status") >= 3);
    }

    @Test
    void matchScoreReturnsZeroForNonMatch() {
        assertEquals(0, fuzzySearcher.matchScore("zz", "git status"));
    }

    @Test
    void matchScoreReturnsZeroWhenQueryCannotFitCandidate() {
        assertEquals(0, fuzzySearcher.matchScore("gitstatus", "git"));
    }

    @Test
    void searchReturnsMatchingGitStatusAndGitStash() {
        List<String> history = List.of("git status", "git commit", "create notes.txt", "git stash");

        List<String> results = fuzzySearcher.search("gst", history);

        assertTrue(results.contains("git status"));
        assertTrue(results.contains("git stash"));
        assertFalse(results.contains("create notes.txt"));
    }

    @Test
    void searchRanksGitStatusBeforeGitStashForGst() {
        List<String> history = List.of("git stash", "git status", "git commit", "create notes.txt");

        List<String> results = fuzzySearcher.search("gst", history);

        assertTrue(results.indexOf("git status") < results.indexOf("git stash"));
    }

    @Test
    void searchReturnsEmptyListForNullQuery() {
        assertTrue(fuzzySearcher.search(null, List.of("git status")).isEmpty());
    }

    @Test
    void searchReturnsEmptyListForEmptyHistory() {
        assertTrue(fuzzySearcher.search("gst", List.of()).isEmpty());
    }

    @Test
    void searchReturnsAtMostTenResults() {
        List<String> history = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            history.add("git status " + i);
        }

        List<String> results = fuzzySearcher.search("gst", history);

        assertEquals(10, results.size());
    }

    @Test
    void searchDoesNotModifyOriginalHistoryList() {
        List<String> history = new ArrayList<>(List.of("git status", "git commit", "create notes.txt"));
        List<String> original = new ArrayList<>(history);

        fuzzySearcher.search("git", history);

        assertEquals(original, history);
    }

    @Test
    void searchWithScoresReturnsSearchResultFields() {
        List<String> history = List.of("git status");

        List<FuzzySearcher.SearchResult> results = fuzzySearcher.searchWithScores("gst", history);

        assertEquals("git status", results.get(0).command);
        assertEquals(fuzzySearcher.matchScore("gst", "git status"), results.get(0).score);
    }

    @Test
    void searchResultToStringUsesExpectedFormat() {
        FuzzySearcher.SearchResult result = new FuzzySearcher.SearchResult("git status", 2);

        assertEquals("[2] git status", result.toString());
    }

    @Test
    void searchWithScoresSortsByScoreDescending() {
        List<String> history = List.of("git stash", "git status", "g s t");

        List<FuzzySearcher.SearchResult> results = fuzzySearcher.searchWithScores("gst", history);

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).score >= results.get(i + 1).score);
        }
    }
}
