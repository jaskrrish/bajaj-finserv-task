package com.bajaj.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submission_records")
public class SubmissionRecord {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false, unique = true)
    private QuizRun run;

    @Column(name = "request_payload", nullable = false, columnDefinition = "text")
    private String requestPayload;

    @Column(name = "response_payload", nullable = false, columnDefinition = "text")
    private String responsePayload;

    @Column(name = "submitted_total", nullable = false)
    private int submittedTotal;

    @Column(name = "expected_total")
    private Integer expectedTotal;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "is_idempotent", nullable = false)
    private boolean idempotent;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    public SubmissionRecord() {
    }

    public SubmissionRecord(
            QuizRun run,
            String requestPayload,
            String responsePayload,
            int submittedTotal,
            Integer expectedTotal,
            boolean correct,
            boolean idempotent,
            String message
    ) {
        this.id = UUID.randomUUID();
        this.run = run;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.submittedTotal = submittedTotal;
        this.expectedTotal = expectedTotal;
        this.correct = correct;
        this.idempotent = idempotent;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public QuizRun getRun() {
        return run;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public int getSubmittedTotal() {
        return submittedTotal;
    }

    public Integer getExpectedTotal() {
        return expectedTotal;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public String getMessage() {
        return message;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
