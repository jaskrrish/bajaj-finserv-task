package com.bajaj.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "deduped_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_deduped_events_run_round_participant",
                columnNames = {"run_id", "round_id", "participant"}
        )
)
public class DedupedEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private QuizRun run;

    @Column(name = "round_id", nullable = false, length = 64)
    private String roundId;

    @Column(nullable = false, length = 255)
    private String participant;

    @Column(nullable = false)
    private int score;

    @Column(name = "source_poll_index", nullable = false)
    private int sourcePollIndex;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    public DedupedEvent() {
    }

    public DedupedEvent(QuizRun run, String roundId, String participant, int score, int sourcePollIndex) {
        this.id = UUID.randomUUID();
        this.run = run;
        this.roundId = roundId;
        this.participant = participant;
        this.score = score;
        this.sourcePollIndex = sourcePollIndex;
    }

    @PrePersist
    void onCreate() {
        this.ingestedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QuizRun getRun() {
        return run;
    }

    public String getRoundId() {
        return roundId;
    }

    public String getParticipant() {
        return participant;
    }

    public int getScore() {
        return score;
    }

    public int getSourcePollIndex() {
        return sourcePollIndex;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }
}
