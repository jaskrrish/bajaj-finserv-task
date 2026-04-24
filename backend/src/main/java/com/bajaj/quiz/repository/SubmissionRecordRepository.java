package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.SubmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRecordRepository extends JpaRepository<SubmissionRecord, UUID> {

    Optional<SubmissionRecord> findByRunId(UUID runId);

    boolean existsByRunId(UUID runId);
}
