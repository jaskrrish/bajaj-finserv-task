package com.bajaj.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "leaderboard_entries")
public class LeaderboardEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private QuizRun run;

    @Column(nullable = false, length = 255)
    private String participant;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(QuizRun run, String participant, int totalScore, int rankOrder) {
        this.id = UUID.randomUUID();
        this.run = run;
        this.participant = participant;
        this.totalScore = totalScore;
        this.rankOrder = rankOrder;
    }

    public UUID getId() {
        return id;
    }

    public QuizRun getRun() {
        return run;
    }

    public String getParticipant() {
        return participant;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getRankOrder() {
        return rankOrder;
    }
}
