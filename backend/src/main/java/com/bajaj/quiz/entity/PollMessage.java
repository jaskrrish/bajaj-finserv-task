package com.bajaj.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "poll_messages")
public class PollMessage {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private QuizRun run;

    @Column(name = "poll_index", nullable = false)
    private int pollIndex;

    @Column(name = "set_id", length = 128)
    private String setId;

    @Column(name = "events_count", nullable = false)
    private int eventsCount;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "text")
    private String rawPayload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public PollMessage() {
    }

    public PollMessage(QuizRun run, int pollIndex, String setId, int eventsCount, String rawPayload) {
        this.id = UUID.randomUUID();
        this.run = run;
        this.pollIndex = pollIndex;
        this.setId = setId;
        this.eventsCount = eventsCount;
        this.rawPayload = rawPayload;
    }

    @PrePersist
    void onCreate() {
        this.receivedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QuizRun getRun() {
        return run;
    }

    public int getPollIndex() {
        return pollIndex;
    }

    public String getSetId() {
        return setId;
    }

    public int getEventsCount() {
        return eventsCount;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
