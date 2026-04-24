package com.bajaj.quiz.service;

import java.time.Duration;

public interface DelayStrategy {

    void sleep(Duration duration);
}
