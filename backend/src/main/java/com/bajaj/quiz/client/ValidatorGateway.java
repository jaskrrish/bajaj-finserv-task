package com.bajaj.quiz.client;

public interface ValidatorGateway {

    ValidatorModels.QuizMessagesResponse fetchQuizMessages(String regNo, int pollIndex);

    ValidatorModels.QuizSubmitResponse submitLeaderboard(ValidatorModels.QuizSubmitRequest request);
}
