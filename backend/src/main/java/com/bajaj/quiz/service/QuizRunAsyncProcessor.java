package com.bajaj.quiz.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QuizRunAsyncProcessor {

    private final QuizRunService quizRunService;

    public QuizRunAsyncProcessor(QuizRunService quizRunService) {
        this.quizRunService = quizRunService;
    }

    @Async
    public void orchestrate(UUID runId) {
        try {
            quizRunService.executePolling(runId);
        } catch (Exception exception) {
            quizRunService.markFailed(runId, exception);
        }
    }
}
