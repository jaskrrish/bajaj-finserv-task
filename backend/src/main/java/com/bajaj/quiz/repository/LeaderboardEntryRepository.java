package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, UUID> {

    List<LeaderboardEntry> findByRunIdOrderByRankOrderAsc(UUID runId);

    void deleteByRunId(UUID runId);
}
