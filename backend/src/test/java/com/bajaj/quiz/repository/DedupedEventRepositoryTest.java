package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.DedupedEvent;
import com.bajaj.quiz.entity.QuizRun;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dedupe;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.validator.base-url=http://localhost"
})
class DedupedEventRepositoryTest {

    @Autowired
    private DedupedEventRepository dedupedEventRepository;

    @Autowired
    private QuizRunRepository quizRunRepository;

    @Test
    void rejectsDuplicateRoundAndParticipantWithinSameRun() {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));
        dedupedEventRepository.saveAndFlush(new DedupedEvent(run, "R1", "Alice", 10, 0));

        assertThatThrownBy(() -> dedupedEventRepository.saveAndFlush(new DedupedEvent(run, "R1", "Alice", 10, 3)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
