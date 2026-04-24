package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.DedupedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DedupedEventRepository extends JpaRepository<DedupedEvent, UUID> {

    List<DedupedEvent> findByRunIdOrderByParticipantAscRoundIdAsc(UUID runId);

    long countByRunId(UUID runId);

    boolean existsByRunIdAndRoundIdAndParticipant(UUID runId, String roundId, String participant);

    @Query("select coalesce(sum(e.score), 0) from DedupedEvent e where e.run.id = :runId")
    int sumScoresByRunId(UUID runId);
}
