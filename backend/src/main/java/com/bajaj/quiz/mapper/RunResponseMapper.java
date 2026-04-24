package com.bajaj.quiz.mapper;

import com.bajaj.quiz.dto.ApiDtos;
import com.bajaj.quiz.entity.DedupedEvent;
import com.bajaj.quiz.entity.LeaderboardEntry;
import com.bajaj.quiz.entity.PollMessage;
import com.bajaj.quiz.entity.QuizRun;
import com.bajaj.quiz.entity.SubmissionRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RunResponseMapper {

    public ApiDtos.RunListItem toListItem(QuizRun run) {
        return new ApiDtos.RunListItem(
                run.getId(),
                run.getRegNo(),
                run.getStatus(),
                run.getPollsCompleted(),
                run.getUniqueEvents(),
                run.getDuplicateEvents(),
                run.getTotalScore(),
                run.getCreatedAt(),
                run.getCompletedAt()
        );
    }

    public ApiDtos.RunSummaryResponse toSummary(
            QuizRun run,
            List<PollMessage> polls,
            List<DedupedEvent> dedupedEvents,
            List<LeaderboardEntry> leaderboard,
            SubmissionRecord submissionRecord
    ) {
        return new ApiDtos.RunSummaryResponse(
                run.getId(),
                run.getRegNo(),
                run.getSetId(),
                run.getStatus(),
                run.getPollsCompleted(),
                run.getUniqueEvents(),
                run.getDuplicateEvents(),
                run.getTotalScore(),
                run.getFailureReason(),
                run.getCreatedAt(),
                run.getUpdatedAt(),
                run.getCompletedAt(),
                polls.stream()
                        .map(poll -> new ApiDtos.PollMessageView(
                                poll.getId(),
                                poll.getPollIndex(),
                                poll.getSetId(),
                                poll.getEventsCount(),
                                poll.getRawPayload(),
                                poll.getReceivedAt()
                        ))
                        .toList(),
                dedupedEvents.stream()
                        .map(event -> new ApiDtos.DedupedEventView(
                                event.getRoundId(),
                                event.getParticipant(),
                                event.getScore(),
                                event.getSourcePollIndex(),
                                event.getIngestedAt()
                        ))
                        .toList(),
                leaderboard.stream()
                        .map(entry -> new ApiDtos.LeaderboardEntryView(
                                entry.getRankOrder(),
                                entry.getParticipant(),
                                entry.getTotalScore()
                        ))
                        .toList(),
                submissionRecord == null
                        ? null
                        : new ApiDtos.SubmissionRecordView(
                        submissionRecord.isCorrect(),
                        submissionRecord.isIdempotent(),
                        submissionRecord.getSubmittedTotal(),
                        submissionRecord.getExpectedTotal(),
                        submissionRecord.getMessage(),
                        submissionRecord.getSubmittedAt(),
                        submissionRecord.getRequestPayload(),
                        submissionRecord.getResponsePayload()
                )
        );
    }
}
