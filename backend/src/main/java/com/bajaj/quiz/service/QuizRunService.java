package com.bajaj.quiz.service;

import com.bajaj.quiz.client.ValidatorGateway;
import com.bajaj.quiz.client.ValidatorModels;
import com.bajaj.quiz.config.AppProperties;
import com.bajaj.quiz.domain.RunStatus;
import com.bajaj.quiz.dto.ApiDtos;
import com.bajaj.quiz.entity.DedupedEvent;
import com.bajaj.quiz.entity.LeaderboardEntry;
import com.bajaj.quiz.entity.PollMessage;
import com.bajaj.quiz.entity.QuizRun;
import com.bajaj.quiz.entity.SubmissionRecord;
import com.bajaj.quiz.mapper.RunResponseMapper;
import com.bajaj.quiz.repository.DedupedEventRepository;
import com.bajaj.quiz.repository.LeaderboardEntryRepository;
import com.bajaj.quiz.repository.PollMessageRepository;
import com.bajaj.quiz.repository.QuizRunRepository;
import com.bajaj.quiz.repository.SubmissionRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class QuizRunService {

    private final QuizRunRepository quizRunRepository;
    private final PollMessageRepository pollMessageRepository;
    private final DedupedEventRepository dedupedEventRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final SubmissionRecordRepository submissionRecordRepository;
    private final ValidatorGateway validatorGateway;
    private final LeaderboardCalculator leaderboardCalculator;
    private final LeaderboardExportPort leaderboardExportPort;
    private final RunResponseMapper runResponseMapper;
    private final ObjectMapper objectMapper;
    private final DelayStrategy delayStrategy;
    private final AppProperties appProperties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public QuizRunService(
            QuizRunRepository quizRunRepository,
            PollMessageRepository pollMessageRepository,
            DedupedEventRepository dedupedEventRepository,
            LeaderboardEntryRepository leaderboardEntryRepository,
            SubmissionRecordRepository submissionRecordRepository,
            ValidatorGateway validatorGateway,
            LeaderboardCalculator leaderboardCalculator,
            LeaderboardExportPort leaderboardExportPort,
            RunResponseMapper runResponseMapper,
            ObjectMapper objectMapper,
            DelayStrategy delayStrategy,
            AppProperties appProperties,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.quizRunRepository = quizRunRepository;
        this.pollMessageRepository = pollMessageRepository;
        this.dedupedEventRepository = dedupedEventRepository;
        this.leaderboardEntryRepository = leaderboardEntryRepository;
        this.submissionRecordRepository = submissionRecordRepository;
        this.validatorGateway = validatorGateway;
        this.leaderboardCalculator = leaderboardCalculator;
        this.leaderboardExportPort = leaderboardExportPort;
        this.runResponseMapper = runResponseMapper;
        this.objectMapper = objectMapper;
        this.delayStrategy = delayStrategy;
        this.appProperties = appProperties;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public ApiDtos.StartRunResponse startRun(String regNo) {
        QuizRun run = quizRunRepository.save(QuizRun.running(regNo));
        return new ApiDtos.StartRunResponse(run.getId(), run.getStatus());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.RunListItem> getRuns() {
        return quizRunRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(runResponseMapper::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiDtos.RunSummaryResponse getRun(UUID runId) {
        QuizRun run = quizRunRepository.findById(runId).orElseThrow(() -> notFound(runId));
        return buildSummary(run);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.PollMessageView> getPolls(UUID runId) {
        ensureRunExists(runId);
        return pollMessageRepository.findByRunIdOrderByPollIndexAsc(runId).stream()
                .map(poll -> new ApiDtos.PollMessageView(
                        poll.getId(),
                        poll.getPollIndex(),
                        poll.getSetId(),
                        poll.getEventsCount(),
                        poll.getRawPayload(),
                        poll.getReceivedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.LeaderboardEntryView> getLeaderboard(UUID runId) {
        ensureRunExists(runId);
        return leaderboardEntryRepository.findByRunIdOrderByRankOrderAsc(runId).stream()
                .map(entry -> new ApiDtos.LeaderboardEntryView(entry.getRankOrder(), entry.getParticipant(), entry.getTotalScore()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApiDtos.SubmissionRecordView getSubmission(UUID runId) {
        ensureRunExists(runId);
        return submissionRecordRepository.findByRunId(runId)
                .map(record -> new ApiDtos.SubmissionRecordView(
                        record.isCorrect(),
                        record.isIdempotent(),
                        record.getSubmittedTotal(),
                        record.getExpectedTotal(),
                        record.getMessage(),
                        record.getSubmittedAt(),
                        record.getRequestPayload(),
                        record.getResponsePayload()
                ))
                .orElse(null);
    }

    public void executePolling(UUID runId) {
        String regNo = transactionTemplate.execute(status -> quizRunRepository.findById(runId)
                .orElseThrow(() -> notFound(runId))
                .getRegNo());

        for (int pollIndex = 0; pollIndex < 10; pollIndex++) {
            ValidatorModels.QuizMessagesResponse response = validatorGateway.fetchQuizMessages(regNo, pollIndex);
            transactionTemplate.executeWithoutResult(status -> persistPollResponse(runId, response));
            if (pollIndex < 9) {
                delayStrategy.sleep(Duration.ofMillis(appProperties.validator().pollDelayMs()));
            }
        }

        Map<String, Integer> totals = transactionTemplate.execute(status -> finalizeRun(runId));
        transactionTemplate.executeWithoutResult(status -> submitOnce(runId, totals));
        ApiDtos.RunSummaryResponse summary = transactionTemplate.execute(status -> {
            QuizRun run = quizRunRepository.findById(runId).orElseThrow(() -> notFound(runId));
            return buildSummary(run);
        });
        leaderboardExportPort.export(summary);
    }

    @Transactional
    public void persistPollResponse(UUID runId, ValidatorModels.QuizMessagesResponse response) {
        QuizRun run = quizRunRepository.findById(runId).orElseThrow(() -> notFound(runId));
        run.setSetId(response.setId());
        run.setPollsCompleted(response.pollIndex() + 1);

        List<ValidatorModels.QuizEvent> events = response.events() == null ? List.of() : response.events();
        pollMessageRepository.save(new PollMessage(
                run,
                response.pollIndex(),
                response.setId(),
                events.size(),
                writeJson(response)
        ));

        for (ValidatorModels.QuizEvent event : events) {
            if (!dedupedEventRepository.existsByRunIdAndRoundIdAndParticipant(
                    runId,
                    event.roundId(),
                    event.participant()
            )) {
                dedupedEventRepository.saveAndFlush(new DedupedEvent(
                        run,
                        event.roundId(),
                        event.participant(),
                        event.score(),
                        response.pollIndex()
                ));
                run.setUniqueEvents(run.getUniqueEvents() + 1);
            } else {
                run.setDuplicateEvents(run.getDuplicateEvents() + 1);
            }
        }

        quizRunRepository.save(run);
    }

    @Transactional
    public Map<String, Integer> finalizeRun(UUID runId) {
        QuizRun run = quizRunRepository.findById(runId).orElseThrow(() -> notFound(runId));
        List<DedupedEvent> dedupedEvents = dedupedEventRepository.findByRunIdOrderByParticipantAscRoundIdAsc(runId);
        Map<String, Integer> totals = leaderboardCalculator.aggregateScores(dedupedEvents);

        leaderboardEntryRepository.deleteByRunId(runId);
        int rank = 1;
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            leaderboardEntryRepository.save(new LeaderboardEntry(run, entry.getKey(), entry.getValue(), rank++));
        }

        int totalScore = totals.values().stream().mapToInt(Integer::intValue).sum();
        run.setTotalScore(totalScore);
        run.setStatus(RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now(clock));
        quizRunRepository.save(run);
        return totals;
    }

    @Transactional
    public void submitOnce(UUID runId, Map<String, Integer> totals) {
        QuizRun run = quizRunRepository.findById(runId).orElseThrow(() -> notFound(runId));
        if (submissionRecordRepository.existsByRunId(runId)) {
            return;
        }

        ValidatorModels.QuizSubmitRequest request = new ValidatorModels.QuizSubmitRequest(
                run.getRegNo(),
                leaderboardCalculator.toSubmissionLeaderboard(totals)
        );
        ValidatorModels.QuizSubmitResponse response = validatorGateway.submitLeaderboard(request);
        int submittedTotal = response.submittedTotal() == null
                ? totals.values().stream().mapToInt(Integer::intValue).sum()
                : response.submittedTotal();
        Integer expectedTotal = response.expectedTotal();
        boolean correct = response.isCorrect() != null
                ? response.isCorrect()
                : expectedTotal != null && Objects.equals(submittedTotal, expectedTotal);
        boolean idempotent = response.isIdempotent() != null
                ? response.isIdempotent()
                : response.attemptCount() != null && response.attemptCount() > 1;

        submissionRecordRepository.save(new SubmissionRecord(
                run,
                writeJson(request),
                writeJson(response),
                submittedTotal,
                expectedTotal,
                correct,
                idempotent,
                buildSubmissionMessage(response)
        ));
    }

    @Transactional
    public void markFailed(UUID runId, Exception exception) {
        QuizRun run = quizRunRepository.findById(runId).orElse(null);
        if (run == null) {
            return;
        }
        run.setStatus(RunStatus.FAILED);
        run.setFailureReason(exception.getMessage());
        run.setCompletedAt(Instant.now(clock));
        quizRunRepository.save(run);
    }

    private ApiDtos.RunSummaryResponse buildSummary(QuizRun run) {
        return runResponseMapper.toSummary(
                run,
                pollMessageRepository.findByRunIdOrderByPollIndexAsc(run.getId()),
                dedupedEventRepository.findByRunIdOrderByParticipantAscRoundIdAsc(run.getId()),
                leaderboardEntryRepository.findByRunIdOrderByRankOrderAsc(run.getId()),
                submissionRecordRepository.findByRunId(run.getId()).orElse(null)
        );
    }

    private void ensureRunExists(UUID runId) {
        if (!quizRunRepository.existsById(runId)) {
            throw notFound(runId);
        }
    }

    private EntityNotFoundException notFound(UUID runId) {
        return new EntityNotFoundException("Run not found: " + runId);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize payload", exception);
        }
    }

    private String buildSubmissionMessage(ValidatorModels.QuizSubmitResponse response) {
        if (response.message() != null && !response.message().isBlank()) {
            return response.message();
        }
        if (response.attemptCount() != null) {
            return "Submission accepted by validator on attempt " + response.attemptCount() + ".";
        }
        return "Submission accepted by validator.";
    }
}
