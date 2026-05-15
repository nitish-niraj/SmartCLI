package com.lpu.smartcli;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.lpu.smartcli.smart.FuzzySearcher;

public class FuzzySearcherTest {

    @Test
    public void testFuzzyMatchAndScore() {
        FuzzySearcher s = new FuzzySearcher();
        assertTrue(s.fuzzyMatch("git", "git status"));
        assertTrue(s.fuzzyMatch("gst", "git status"));
        assertFalse(s.fuzzyMatch("xyz", "git status"));
        assertTrue(s.matchScore("git", "git status") > 0);
        assertEquals(0, s.matchScore("xyz", "git status"));
    }

    @Test
    public void testSearchReturnsOrderedResults() {
        FuzzySearcher s = new FuzzySearcher();
        List<String> history = List.of("git status", "git commit -m msg", "ls -la", "git add .");
        List<String> results = s.search("git", history, 10);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).contains("git"));
    }
}
