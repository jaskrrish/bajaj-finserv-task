package com.bajaj.quiz.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestValidatorGateway implements ValidatorGateway {

    private final RestClient restClient;

    public RestValidatorGateway(RestClient validatorRestClient) {
        this.restClient = validatorRestClient;
    }

    @Override
    public ValidatorModels.QuizMessagesResponse fetchQuizMessages(String regNo, int pollIndex) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/quiz/messages")
                        .queryParam("regNo", regNo)
                        .queryParam("poll", pollIndex)
                        .build())
                .retrieve()
                .body(ValidatorModels.QuizMessagesResponse.class);
    }

    @Override
    public ValidatorModels.QuizSubmitResponse submitLeaderboard(ValidatorModels.QuizSubmitRequest request) {
        return restClient.post()
                .uri("/quiz/submit")
                .body(request)
                .retrieve()
                .body(ValidatorModels.QuizSubmitResponse.class);
    }
}
