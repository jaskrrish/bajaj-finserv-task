package com.bajaj.quiz.service;

import com.bajaj.quiz.client.ValidatorGateway;
import com.bajaj.quiz.client.ValidatorModels;
import com.bajaj.quiz.domain.RunStatus;
import com.bajaj.quiz.dto.ApiDtos;
import com.bajaj.quiz.entity.QuizRun;
import com.bajaj.quiz.repository.LeaderboardEntryRepository;
import com.bajaj.quiz.repository.PollMessageRepository;
import com.bajaj.quiz.repository.QuizRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:service;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.validator.base-url=http://localhost",
        "app.validator.poll-delay-ms=1"
})
class QuizRunServiceTest {

    @Autowired
    private QuizRunService quizRunService;

    @Autowired
    private QuizRunRepository quizRunRepository;

    @Autowired
    private PollMessageRepository pollMessageRepository;

    @Autowired
    private LeaderboardEntryRepository leaderboardEntryRepository;

    @Autowired
    private LeaderboardCalculator leaderboardCalculator;

    @Autowired
    private StubValidatorGateway validatorGateway;

    @Autowired
    private RecordingDelayStrategy delayStrategy;

    @Autowired
    private RecordingExportPort exportPort;

    @BeforeEach
    void setUp() {
        validatorGateway.reset();
        delayStrategy.reset();
        exportPort.reset();
    }

    @Test
    void executesTenPollsWaitsBetweenThemAndSubmitsOnce() {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));

        quizRunService.executePolling(run.getId());

        QuizRun persisted = quizRunRepository.findById(run.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(RunStatus.COMPLETED);
        assertThat(persisted.getPollsCompleted()).isEqualTo(10);
        assertThat(persisted.getUniqueEvents()).isEqualTo(3);
        assertThat(persisted.getDuplicateEvents()).isEqualTo(1);
        assertThat(persisted.getTotalScore()).isEqualTo(60);
        assertThat(validatorGateway.fetchPolls).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(delayStrategy.sleepCalls).hasSize(9);
        assertThat(validatorGateway.submitCalls).isEqualTo(1);
        assertThat(exportPort.exportCalls).isEqualTo(1);
    }

    @Test
    void doesNotSubmitTwiceForSameRun() {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));
        quizRunService.executePolling(run.getId());

        QuizRun persisted = quizRunRepository.findById(run.getId()).orElseThrow();
        Map<String, Integer> totals = leaderboardCalculator.aggregateScores(
                List.of(
                        new com.bajaj.quiz.entity.DedupedEvent(persisted, "R1", "Alice", 10, 0),
                        new com.bajaj.quiz.entity.DedupedEvent(persisted, "R1", "Bob", 20, 0),
                        new com.bajaj.quiz.entity.DedupedEvent(persisted, "R2", "Alice", 30, 2)
                )
        );

        quizRunService.submitOnce(persisted.getId(), totals);

        assertThat(validatorGateway.submitCalls).isEqualTo(1);
    }

    @Test
    void preservesLeaderboardStateWhenSubmitFails() {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));
        validatorGateway.failSubmit = true;

        Exception failure = null;
        try {
            quizRunService.executePolling(run.getId());
        } catch (Exception exception) {
            failure = exception;
            quizRunService.markFailed(run.getId(), exception);
        }

        assertThat(failure).isNotNull();

        QuizRun persisted = quizRunRepository.findById(run.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(persisted.getPollsCompleted()).isEqualTo(10);
        assertThat(persisted.getTotalScore()).isEqualTo(60);
        assertThat(leaderboardEntryRepository.findByRunIdOrderByRankOrderAsc(run.getId())).hasSize(2);
    }

    @Test
    void commitsPollProgressBeforeWholeSequenceCompletes() throws InterruptedException {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));
        delayStrategy.blockOnNextSleep();

        Thread worker = Thread.ofVirtual().start(() -> quizRunService.executePolling(run.getId()));

        assertThat(delayStrategy.awaitBlockedSleep()).isTrue();
        assertThat(quizRunRepository.findById(run.getId()).orElseThrow().getPollsCompleted()).isEqualTo(1);
        assertThat(pollMessageRepository.findByRunIdOrderByPollIndexAsc(run.getId())).hasSize(1);

        delayStrategy.releaseBlockedSleep();
        worker.join(5_000);

        assertThat(worker.isAlive()).isFalse();
        assertThat(quizRunRepository.findById(run.getId()).orElseThrow().getPollsCompleted()).isEqualTo(10);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        StubValidatorGateway stubValidatorGateway() {
            return new StubValidatorGateway();
        }

        @Bean
        @Primary
        RecordingDelayStrategy recordingDelayStrategy() {
            return new RecordingDelayStrategy();
        }

        @Bean
        @Primary
        RecordingExportPort recordingExportPort() {
            return new RecordingExportPort();
        }
    }

    static class StubValidatorGateway implements ValidatorGateway {
        final List<Integer> fetchPolls = new ArrayList<>();
        int submitCalls;
        boolean failSubmit;

        @Override
        public ValidatorModels.QuizMessagesResponse fetchQuizMessages(String regNo, int pollIndex) {
            fetchPolls.add(pollIndex);
            List<ValidatorModels.QuizEvent> events = switch (pollIndex) {
                case 0 -> List.of(
                        new ValidatorModels.QuizEvent("R1", "Alice", 10),
                        new ValidatorModels.QuizEvent("R1", "Bob", 20)
                );
                case 1 -> List.of(new ValidatorModels.QuizEvent("R1", "Alice", 10));
                case 2 -> List.of(new ValidatorModels.QuizEvent("R2", "Alice", 30));
                default -> List.of();
            };
            return new ValidatorModels.QuizMessagesResponse(regNo, "SET_1", pollIndex, events);
        }

        @Override
        public ValidatorModels.QuizSubmitResponse submitLeaderboard(ValidatorModels.QuizSubmitRequest request) {
            submitCalls++;
            if (failSubmit) {
                throw new IllegalStateException("submit failed");
            }
            return new ValidatorModels.QuizSubmitResponse(
                    request.regNo(),
                    10,
                    60,
                    submitCalls,
                    true,
                    submitCalls > 1,
                    60,
                    "Correct!"
            );
        }

        void reset() {
            fetchPolls.clear();
            submitCalls = 0;
            failSubmit = false;
        }
    }

    static class RecordingDelayStrategy implements DelayStrategy {
        final List<Duration> sleepCalls = new ArrayList<>();
        private volatile boolean shouldBlockNextSleep;
        private volatile CountDownLatch blockedSleepEntered;
        private volatile CountDownLatch releaseBlockedSleep;

        @Override
        public void sleep(Duration duration) {
            sleepCalls.add(duration);
            CountDownLatch entered = blockedSleepEntered;
            CountDownLatch release = releaseBlockedSleep;
            if (shouldBlockNextSleep && entered != null && release != null) {
                shouldBlockNextSleep = false;
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting to release blocked sleep");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting to release blocked sleep", exception);
                }
            }
        }

        void reset() {
            sleepCalls.clear();
            shouldBlockNextSleep = false;
            blockedSleepEntered = null;
            releaseBlockedSleep = null;
        }

        void blockOnNextSleep() {
            shouldBlockNextSleep = true;
            blockedSleepEntered = new CountDownLatch(1);
            releaseBlockedSleep = new CountDownLatch(1);
        }

        boolean awaitBlockedSleep() throws InterruptedException {
            CountDownLatch latch = blockedSleepEntered;
            return latch != null && latch.await(2, TimeUnit.SECONDS);
        }

        void releaseBlockedSleep() {
            CountDownLatch latch = releaseBlockedSleep;
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    static class RecordingExportPort implements LeaderboardExportPort {
        int exportCalls;

        @Override
        public void export(ApiDtos.RunSummaryResponse summaryResponse) {
            exportCalls++;
        }

        void reset() {
            exportCalls = 0;
        }
    }
}
