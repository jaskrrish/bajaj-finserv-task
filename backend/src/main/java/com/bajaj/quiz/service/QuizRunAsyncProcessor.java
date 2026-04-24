package com.bajaj.quiz.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executor;

@Component
public class QuizRunAsyncProcessor {

    private final QuizRunService quizRunService;
    private final Executor quizRunExecutor;

    public QuizRunAsyncProcessor(QuizRunService quizRunService, Executor quizRunExecutor) {
        this.quizRunService = quizRunService;
        this.quizRunExecutor = quizRunExecutor;
    }

    public void orchestrate(UUID runId) {
        quizRunExecutor.execute(() -> {
            try {
                quizRunService.executePolling(runId);
            } catch (Exception exception) {
                quizRunService.markFailed(runId, exception);
            }
        });
    }
}
