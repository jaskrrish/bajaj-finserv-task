package com.bajaj.quiz.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QuizRunAsyncProcessor {

    private final QuizRunService quizRunService;

    public QuizRunAsyncProcessor(QuizRunService quizRunService) {
        this.quizRunService = quizRunService;
    }

    public void orchestrate(UUID runId) {
        Thread.ofVirtual().name("quiz-run-" + runId).start(() -> {
            try {
                quizRunService.executePolling(runId);
            } catch (Exception exception) {
                quizRunService.markFailed(runId, exception);
            }
        });
    }
}
