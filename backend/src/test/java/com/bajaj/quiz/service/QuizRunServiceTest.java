package com.bajaj.quiz.service;

import com.bajaj.quiz.client.ValidatorGateway;
import com.bajaj.quiz.client.ValidatorModels;
import com.bajaj.quiz.domain.RunStatus;
import com.bajaj.quiz.dto.ApiDtos;
import com.bajaj.quiz.entity.QuizRun;
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

        quizRunService.submitOnce(persisted, totals);

        assertThat(validatorGateway.submitCalls).isEqualTo(1);
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
            return new ValidatorModels.QuizSubmitResponse(true, true, 60, 60, "Correct!");
        }

        void reset() {
            fetchPolls.clear();
            submitCalls = 0;
        }
    }

    static class RecordingDelayStrategy implements DelayStrategy {
        final List<Duration> sleepCalls = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            sleepCalls.add(duration);
        }

        void reset() {
            sleepCalls.clear();
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
