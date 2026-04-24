package com.bajaj.quiz.dto;

import com.bajaj.quiz.domain.RunStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record StartRunRequest(
            @NotBlank(message = "regNo is required")
            String regNo
    ) {
    }

    public record StartRunResponse(
            UUID runId,
            RunStatus status
    ) {
    }

    public record RunListItem(
            UUID runId,
            String regNo,
            RunStatus status,
            int pollsCompleted,
            int uniqueEvents,
            int duplicateEvents,
            int totalScore,
            Instant createdAt,
            Instant completedAt
    ) {
    }

    public record PollMessageView(
            UUID id,
            int pollIndex,
            String setId,
            int eventsCount,
            String rawPayload,
            Instant receivedAt
    ) {
    }

    public record LeaderboardEntryView(
            int rank,
            String participant,
            int totalScore
    ) {
    }

    public record SubmissionRecordView(
            boolean correct,
            boolean idempotent,
            int submittedTotal,
            Integer expectedTotal,
            String message,
            Instant submittedAt,
            String requestPayload,
            String responsePayload
    ) {
    }

    public record DedupedEventView(
            String roundId,
            String participant,
            int score,
            int sourcePollIndex,
            Instant ingestedAt
    ) {
    }

    public record RunSummaryResponse(
            UUID runId,
            String regNo,
            String setId,
            RunStatus status,
            int pollsCompleted,
            int uniqueEvents,
            int duplicateEvents,
            int totalScore,
            String failureReason,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt,
            List<PollMessageView> polls,
            List<DedupedEventView> dedupedEvents,
            List<LeaderboardEntryView> leaderboard,
            SubmissionRecordView submission
    ) {
    }
}
