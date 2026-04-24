package com.bajaj.quiz.config;

import com.bajaj.quiz.service.DelayStrategy;
import com.bajaj.quiz.service.ThreadSleepDelayStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    @Bean
    Clock appClock() {
        return Clock.systemUTC();
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    RestClient validatorRestClient(RestClient.Builder builder, AppProperties appProperties) {
        return builder
                .baseUrl(appProperties.validator().baseUrl())
                .build();
    }

    @Bean
    DelayStrategy delayStrategy() {
        return new ThreadSleepDelayStrategy();
    }

    @Bean(name = "quizRunExecutor")
    Executor quizRunExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    WebMvcConfigurer corsConfigurer(AppProperties appProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(appProperties.cors().allowedOrigins().toArray(String[]::new))
                        .allowedMethods("GET", "POST")
                        .allowedHeaders("*");
            }
        };
    }
}
