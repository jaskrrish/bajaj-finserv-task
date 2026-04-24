package com.bajaj.quiz.service;

import com.bajaj.quiz.domain.RunStatus;
import com.bajaj.quiz.entity.QuizRun;
import com.bajaj.quiz.repository.QuizRunRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class QuizRunFlowIntegrationTest {

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(0);

    @Autowired
    private QuizRunService quizRunService;

    @Autowired
    private QuizRunRepository quizRunRepository;

    @BeforeAll
    static void beforeAll() {
        WIRE_MOCK_SERVER.start();
        for (int poll = 0; poll < 10; poll++) {
            String body = switch (poll) {
                case 0 -> """
                        {"regNo":"2024CS101","setId":"SET_1","pollIndex":0,"events":[
                          {"roundId":"R1","participant":"Alice","score":10},
                          {"roundId":"R1","participant":"Bob","score":20}
                        ]}
                        """;
                case 1 -> """
                        {"regNo":"2024CS101","setId":"SET_1","pollIndex":1,"events":[
                          {"roundId":"R1","participant":"Alice","score":10}
                        ]}
                        """;
                case 2 -> """
                        {"regNo":"2024CS101","setId":"SET_1","pollIndex":2,"events":[
                          {"roundId":"R2","participant":"Alice","score":30}
                        ]}
                        """;
                default -> "{\"regNo\":\"2024CS101\",\"setId\":\"SET_1\",\"pollIndex\":" + poll + ",\"events\":[]}";
            };

            WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/quiz/messages"))
                    .withQueryParam("regNo", equalTo("2024CS101"))
                    .withQueryParam("poll", equalTo(String.valueOf(poll)))
                    .willReturn(okJson(body)));
        }

        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/quiz/submit"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"isCorrect":true,"isIdempotent":true,"submittedTotal":60,"expectedTotal":60,"message":"Correct!"}
                                """)));
    }

    @AfterAll
    static void afterAll() {
        WIRE_MOCK_SERVER.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.validator.base-url", WIRE_MOCK_SERVER::baseUrl);
        registry.add("app.validator.poll-delay-ms", () -> 1L);
    }

    @Test
    void callsValidatorTenTimesAndSubmitsOnce() {
        QuizRun run = quizRunRepository.save(QuizRun.running("2024CS101"));

        quizRunService.executePolling(run.getId());

        QuizRun persisted = quizRunRepository.findById(run.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(RunStatus.COMPLETED);
        assertThat(persisted.getTotalScore()).isEqualTo(60);

        WIRE_MOCK_SERVER.verify(10, getRequestedFor(urlPathEqualTo("/quiz/messages")));
        WIRE_MOCK_SERVER.verify(1, postRequestedFor(urlEqualTo("/quiz/submit")));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        DelayStrategy noDelayStrategy() {
            return duration -> {
            };
        }
    }
}
