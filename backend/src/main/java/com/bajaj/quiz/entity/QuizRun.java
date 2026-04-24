package com.bajaj.quiz.entity;

import com.bajaj.quiz.domain.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_runs")
public class QuizRun {

    @Id
    private UUID id;

    @Column(name = "reg_no", nullable = false, length = 64)
    private String regNo;

    @Column(name = "set_id", length = 128)
    private String setId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RunStatus status;

    @Column(name = "polls_completed", nullable = false)
    private int pollsCompleted;

    @Column(name = "unique_events", nullable = false)
    private int uniqueEvents;

    @Column(name = "duplicate_events", nullable = false)
    private int duplicateEvents;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public QuizRun() {
    }

    public static QuizRun running(String regNo) {
        QuizRun run = new QuizRun();
        run.id = UUID.randomUUID();
        run.regNo = regNo;
        run.status = RunStatus.RUNNING;
        run.pollsCompleted = 0;
        run.uniqueEvents = 0;
        run.duplicateEvents = 0;
        run.totalScore = 0;
        return run;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public int getPollsCompleted() {
        return pollsCompleted;
    }

    public void setPollsCompleted(int pollsCompleted) {
        this.pollsCompleted = pollsCompleted;
    }

    public int getUniqueEvents() {
        return uniqueEvents;
    }

    public void setUniqueEvents(int uniqueEvents) {
        this.uniqueEvents = uniqueEvents;
    }

    public int getDuplicateEvents() {
        return duplicateEvents;
    }

    public void setDuplicateEvents(int duplicateEvents) {
        this.duplicateEvents = duplicateEvents;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
