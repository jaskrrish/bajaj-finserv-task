package com.bajaj.quiz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, Validator validator) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Validator(String baseUrl, long pollDelayMs) {
    }
}
