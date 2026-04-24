package com.bajaj.quiz.client;

import java.util.List;

public final class ValidatorModels {

    private ValidatorModels() {
    }

    public record QuizMessagesResponse(
            String regNo,
            String setId,
            int pollIndex,
            List<QuizEvent> events
    ) {
    }

    public record QuizEvent(
            String roundId,
            String participant,
            int score
    ) {
    }

    public record QuizSubmitRequest(
            String regNo,
            List<LeaderboardItem> leaderboard
    ) {
    }

    public record LeaderboardItem(
            String participant,
            int totalScore
    ) {
    }

    public record QuizSubmitResponse(
            boolean isCorrect,
            boolean isIdempotent,
            int submittedTotal,
            Integer expectedTotal,
            String message
    ) {
    }
}
