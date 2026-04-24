package com.bajaj.quiz.service;

import java.time.Duration;

public class ThreadSleepDelayStrategy implements DelayStrategy {

    @Override
    public void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Polling interrupted", exception);
        }
    }
}
