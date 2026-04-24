package com.bajaj.quiz.service;

import com.bajaj.quiz.entity.DedupedEvent;
import com.bajaj.quiz.entity.QuizRun;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardCalculatorTest {

    private final LeaderboardCalculator leaderboardCalculator = new LeaderboardCalculator();

    @Test
    void aggregatesScoresAcrossRoundsAndSortsDescending() {
        QuizRun run = QuizRun.running("2024CS101");
        List<DedupedEvent> events = List.of(
                new DedupedEvent(run, "R1", "Alice", 10, 0),
                new DedupedEvent(run, "R2", "Alice", 40, 1),
                new DedupedEvent(run, "R1", "Bob", 35, 0),
                new DedupedEvent(run, "R3", "Cara", 15, 2)
        );

        Map<String, Integer> totals = leaderboardCalculator.aggregateScores(events);

        assertThat(totals).containsExactly(
                Map.entry("Alice", 50),
                Map.entry("Bob", 35),
                Map.entry("Cara", 15)
        );
    }
}
