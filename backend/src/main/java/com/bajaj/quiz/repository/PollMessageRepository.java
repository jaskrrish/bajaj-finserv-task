package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.PollMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PollMessageRepository extends JpaRepository<PollMessage, UUID> {

    List<PollMessage> findByRunIdOrderByPollIndexAsc(UUID runId);
}
